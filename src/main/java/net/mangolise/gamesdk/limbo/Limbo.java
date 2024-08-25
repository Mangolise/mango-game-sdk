package net.mangolise.gamesdk.limbo;

import net.mangolise.gamesdk.log.Log;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Limbo instances are used to hold players while they wait for the game to start.
 */
public class Limbo {

    private static Instance instance;

    static {
        instance = MinecraftServer.getInstanceManager().createInstanceContainer((IChunkLoader) null);

        instance.eventNode().addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return;
            event.getPlayer().setGameMode(GameMode.SPECTATOR);

            Log.logger().info("Player joined limbo: {}", event.getPlayer().getUsername());

            JoinEvent joinEvent = new JoinEvent(event.getPlayer());
            MinecraftServer.getGlobalEventHandler().call(joinEvent);
        });
    }

    public static Instance limbo() {
        return instance;
    }

    /**
     * Uses the limbo instance to wait for the given number players to join the game.
     * @param playerCount the number of players to wait for
     * @return a future that completes when all players have joined
     */
    public static CompletableFuture<Set<Player>> waitForPlayers(int playerCount) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // funnel all new players to the limbo instance
        GlobalEventHandler eventNode = MinecraftServer.getGlobalEventHandler();

        // when the new players join, check if enough players have joined to trigger the future
        AtomicInteger playerCounter = new AtomicInteger(0);
        AtomicReference<EventListener<Limbo.JoinEvent>> joinEventListener = new AtomicReference<>();
        var joinListener = EventListener.of(Limbo.JoinEvent.class, event -> {
            int newPlayerCount = playerCounter.incrementAndGet();

            if (newPlayerCount == playerCount) {
                // all players have joined, remove our listeners
                eventNode.removeListener(Objects.requireNonNull(joinEventListener.get(), "Event listener was null"));
                future.complete(null);
            }
        });
        joinEventListener.set(joinListener);
        eventNode.addListener(joinListener);

        return wait(future);
    }

    /**
     * Waits for the given future to complete, then returns the set of players in limbo.
     * @param cancel the future to wait for.
     * @return a future that completes with the set of players in limbo once the provided future completes.
     */
    public static CompletableFuture<Set<Player>> wait(CompletableFuture<Void> cancel) {
        CompletableFuture<Set<Player>> future = new CompletableFuture<>();

        // funnel all new players to the limbo instance
        GlobalEventHandler eventNode = MinecraftServer.getGlobalEventHandler();
        EventListener<AsyncPlayerConfigurationEvent> connectListener = EventListener.of(AsyncPlayerConfigurationEvent.class, event -> {
            // when the player joins the server, we put them into limbo until all players have joined
            event.setSpawningInstance(Limbo.limbo());
        });
        eventNode.addListener(connectListener);

        cancel.thenAccept(v -> {
            eventNode.removeListener(connectListener);
            future.complete(limbo().getPlayers());
        });
        return future;
    }

    public static class JoinEvent implements PlayerEvent {

        private final Player player;
        private JoinEvent(Player player) {
            this.player = player;
        }

        @Override
        public @NotNull Player getPlayer() {
            return player;
        }
    }
}
