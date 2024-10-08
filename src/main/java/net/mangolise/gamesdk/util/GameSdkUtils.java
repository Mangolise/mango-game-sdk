package net.mangolise.gamesdk.util;

import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.mangolise.gamesdk.events.CooldownEvent;
import net.mangolise.gamesdk.events.StopCooldownEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.world.DimensionType;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class GameSdkUtils {

    /**
     * Creates a fake uuid for an offline player
     */
    public static UUID createFakeUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer: " + name).getBytes());
    }

    /**
     * Get the configured port from the environment variables.
     * @return the configured port.
     */
    public static int getConfiguredPort() {
        return Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "25565"));
    }

    public static boolean useBungeeCord() {
        return Boolean.parseBoolean(System.getenv().getOrDefault("USE_BUNGEECORD", "false"));
    }

    /**
     * Get the highest block at the given x and z coordinates.
     * @param instance the instance to check.
     * @param x the x coordinate.
     * @param z the z coordinate.
     * @return the y coordinate of the highest block.
     */
    public static int getHighestBlock(Instance instance, int x, int z) {
        DimensionType dimensionType = instance.getCachedDimensionType();
        int y = dimensionType.maxY() - 1;
        int minY = dimensionType.minY();

        for (; instance.getBlock(x, y, z).isAir() && y > minY; y--) { }

        return y;
    }

    /**
     * Get the spawn position for the given instance. The spawn position is the highest block at 0, 0.
     * Plus a little bit of give for the player to spawn on top of the block and not fall through the world.
     * @param instance the instance to get the spawn position for.
     * @return the spawn position.
     */
    public static Pos getSpawnPosition(Instance instance) {
        return new Pos(0.5, getHighestBlock(instance, 0, 0) + 2, 0.5);
    }

    public static PolarLoader getPolarLoaderFromResource(String path) {
        try {
            InputStream file = ClassLoader.getSystemResourceAsStream(path);
            if (file == null) {
                throw new RuntimeException("Could not find world file");
            }

            return new PolarLoader(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showTitle(Player player, int fadeIn, int stay, int fadeOut, Component title, Component subtitle) {
        Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        player.showTitle(Title.title(title, subtitle, times));
    }

    public static void broadcastMessage(Component message) {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    /**
     * Gets whether a block collides with a bounding box
     */
    public static boolean collidesWithBoundingBox(BoundingBox box, Point position, Point blockPos) {
        Point boxStart = box.relativeStart().add(position);
        Point boxEnd = box.relativeEnd().add(position);
        Point blockEnd = blockPos.add(1, 1, 1);

        return blockEnd.x() > boxStart.x() && blockPos.x() < boxEnd.x() &&
                blockEnd.y() > boxStart.y() && blockPos.y() < boxEnd.y() &&
                blockEnd.z() > boxStart.z() && blockPos.z() < boxEnd.z();
    }

    public static void setBlockCancelPacket(Instance instance, Point pos, Block block) {
        instance.setBlock(pos, block);

        // stops all packets for this block
        EventListener<PlayerPacketOutEvent> listener = EventListener.of(PlayerPacketOutEvent.class, e -> {
            if (e.getPacket() instanceof BlockChangePacket packet &&
                    packet.blockPosition().distanceSquared(pos) < 1d &&
                    packet.blockStateId() == block.stateId()) {
                e.setCancelled(true);
            }
        });

        // listens to the packet and stops listening when the next tick starts
        // (packet is sent at the end of this tick)
        MinecraftServer.getGlobalEventHandler().addListener(listener);
        MinecraftServer.getSchedulerManager().scheduleNextTick(() ->
                MinecraftServer.getGlobalEventHandler().removeListener(listener));
    }

    /**
     * use ths function by doing player.eventNode().addListener(cancelOnePacket(...))
     * or by using MinecraftServer.getGlobalEventManager().addListener(cancelOnePacket(...))
     */
    public static EventListener<PlayerPacketOutEvent> cancelOnePacket(Predicate<SendablePacket> predicate) {
        AtomicBoolean hasFinished = new AtomicBoolean(false);

        return EventListener.builder(PlayerPacketOutEvent.class).handler(e -> {
            if (predicate.test(e.getPacket())) {
                hasFinished.set(true);
                e.setCancelled(true);
            }
        }).expireWhen(b -> hasFinished.get()).build();
    }

    /**
     * use ths function by doing player.eventNode().addListener(singleUseEvent(...))
     * or by using MinecraftServer.getGlobalEventManager().addListener(singleUseEvent(...))
     */
    public static <T extends Event> EventListener<T> singleUseEvent(Class<T> clazz, Predicate<T> handler) {
        AtomicBoolean hasFinished = new AtomicBoolean(false);

        return EventListener.builder(clazz).handler(e -> {
            hasFinished.set(handler.test(e));
        }).expireWhen(b -> hasFinished.get()).build();
    }

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    public static String pointToString(Point point) {
        return decimalFormat.format(point.x()) + " " +
                decimalFormat.format(point.y()) + " " +
                decimalFormat.format(point.z());
    }

    /**
     * Capitalises the first letter of a string
     *
     * @deprecated Use {@link ChatUtil#capitaliseFirstLetter(String)} instead
     * @param string The string to capitalise the first letter of
     * @return The string with the first letter capitalised
     */
    @Deprecated(forRemoval = true)
    public static String capitaliseFirstLetter(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
    }

    public static void startCooldown(Player player, String name, Material icon, long timeMs) {
        CooldownEvent event = new CooldownEvent(name, timeMs, icon, player);
        MinecraftServer.getGlobalEventHandler().call(event);
    }

    public static void stopCooldown(Player player, String name) {
        StopCooldownEvent event = new StopCooldownEvent(player, name);
        MinecraftServer.getGlobalEventHandler().call(event);
    }

    public static void strikeLightning(Instance instance, Pos pos) {
        Entity bolt = new Entity(EntityType.LIGHTNING_BOLT);
        bolt.setInstance(instance, pos);
        MinecraftServer.getSchedulerManager().scheduleNextTick(bolt::remove);
    }

    public static ItemEntity dropItem(Instance world, Point pos, ItemStack stack) {
        ItemEntity item = new ItemEntity(stack);
        item.setPickupDelay(Duration.ofMillis(500));
        item.setInstance(world, new Pos(pos));
        return item;
    }

    public static ItemEntity dropItemNaturally(Instance world, Point pos, ItemStack stack) {
        ItemEntity item = dropItem(world, pos, stack);
        double x = ThreadLocalRandom.current().nextDouble();
        double z = ThreadLocalRandom.current().nextDouble();
        item.setVelocity(new Vec(x, 2, z).mul(2));
        return item;
    }

    public static Set<Integer> getBlockIdsAtPlayerFeet(Player p, double yOffset) {
        Pos playerLocation = p.getPosition().add(0, yOffset, 0);
        Set<BlockVec> points = new HashSet<>();
        BoundingBox box = p.getBoundingBox();

        // get the rounded points for all four corners and add them to a set to remove duplicates
        // center isn't needed because the player is less a block wide
        points.add(new BlockVec(playerLocation.add(box.minX(), box.minY(), box.minZ())));
        points.add(new BlockVec(playerLocation.add(box.minX(), box.minY(), box.maxZ())));
        points.add(new BlockVec(playerLocation.add(box.maxX(), box.minY(), box.minZ())));
        points.add(new BlockVec(playerLocation.add(box.maxX(), box.minY(), box.maxZ())));

        Set<Integer> blockIds = new HashSet<>(points.size());
        for (Point point : points) {
            blockIds.add(p.getInstance().getBlock(point).id());
        }

        return blockIds;
    }

    public static Set<Integer> getBlockIdsPlayerIsStandingOnAndAbove(Player p, int upAmount, boolean ignoreFeet) {
        Set<Integer> idSet = new HashSet<>();
        for (int i = ignoreFeet ? 1 : 0; i < upAmount; i++) {
            idSet.addAll(getBlockIdsAtPlayerFeet(p, i-0.1));
        }

        return idSet;
    }
}
