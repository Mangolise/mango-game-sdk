package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

public class PacketDebugFeature implements Game.Feature<Game> {
    private final Set<UUID> players = new HashSet<>();
    private final Map<UUID, Set<String>> ignored = new HashMap<>();
    private static final Set<String> defaultIgnoredPackets = Set.of(
            "SystemChatPacket", "TimeUpdatePacket", "KeepAlivePacket", "ClientPlayerPositionPacket",
            "ClientPlayerRotationPacket", "ClientPlayerPositionAndRotationPacket", "PlayerInfoUpdatePacket",
            "ClientKeepAlivePacket", "EntityAttributesPacket", "EntityMetaDataPacket", "ClientEntityActionPacket",
            "ChunkDataPacket", "ChunkBatchStartPacket", "ChunkBatchFinishedPacket", "ClientChunkBatchReceivedPacket",
            "UnloadChunkPacket", "ClientSettingsPacket"
    );

    private @UnknownNullability EventListener<PlayerPacketEvent> packetEventListener;
    private @UnknownNullability EventListener<PlayerPacketOutEvent> packetOutEventListener;

    @Override
    public void setup(Context<Game> context) {
        MinecraftServer.getCommandManager().register(new PacketListenCommand());

        packetEventListener = EventListener.of(PlayerPacketEvent.class, e -> {
            if (players.contains(e.getPlayer().getUuid())) {
                onPacket(e.getPlayer(), e.getPacket(), null, false);
            }
        });

        packetOutEventListener = EventListener.of(PlayerPacketOutEvent.class, e -> {
            if (players.contains(e.getPlayer().getUuid())) {
                onPacket(e.getPlayer(), null, e.getPacket(), true);
            }
        });
    }

    private void onPacket(Player p, ClientPacket clientPacket, ServerPacket serverPacket, boolean out) {
        Set<String> ignoredPackets = ignored.get(p.getUuid());

        if (out) {
            if (ignoredPackets.contains(serverPacket.getClass().getSimpleName())) {
                return;
            }

            p.sendMessage("\nOUT: " + serverPacket);
        } else {
            if (ignoredPackets.contains(clientPacket.getClass().getSimpleName())) {
                return;
            }

            p.sendMessage("\nIN : " + clientPacket);
        }
    }

    private class PacketListenCommand extends Command {
        public PacketListenCommand() {
            super("packetlisten");

            // TODO: Add permissions
            setCondition((sender, s) -> sender instanceof Player);

            addSyntax(this::executeNoArgs);
            addSyntax(this::toggleIgnored, ArgumentType.StringArray("ignored"));
        }

        private void executeNoArgs(CommandSender sender, CommandContext context) {
            UUID uuid = ((Player)sender).getUuid();
            if (players.contains(uuid)) {
                // don't remove ignored list in case they need it again
                players.remove(uuid);
                sender.sendMessage("Stopped debugging packets");

                if (players.isEmpty()) {
                    MinecraftServer.getGlobalEventHandler().removeListener(packetEventListener);
                    MinecraftServer.getGlobalEventHandler().removeListener(packetOutEventListener);
                }
            }
            else {
                if (players.isEmpty()) {
                    MinecraftServer.getGlobalEventHandler().addListener(packetEventListener);
                    MinecraftServer.getGlobalEventHandler().addListener(packetOutEventListener);
                }

                players.add(uuid);
                ignored.put(uuid, new HashSet<>(defaultIgnoredPackets));
                sender.sendMessage("Started debugging packets");
            }
        }

        private void toggleIgnored(CommandSender sender, CommandContext context) {
            String[] names = context.get("ignored");
            UUID uuid = ((Player)sender).getUuid();

            if (names[0].equalsIgnoreCase("list")) {
                sender.sendMessage(Objects.requireNonNullElse(ignored.get(uuid), defaultIgnoredPackets).toString());
                return;
            }

            if (ignored.containsKey(uuid)) {
                Set<String> ignoredPackets = ignored.get(((Player)sender).getUuid());

                for (String name : names) {
                    if (ignoredPackets.contains(name)) {
                        ignoredPackets.remove(name);
                        sender.sendMessage("No longer ignoring " + name);
                    } else {
                        ignoredPackets.add(name);
                        sender.sendMessage("Ignoring " + name);
                    }
                }
            }
            else {
                sender.sendMessage("todo: make this work without listening first");
            }
        }
    }
}
