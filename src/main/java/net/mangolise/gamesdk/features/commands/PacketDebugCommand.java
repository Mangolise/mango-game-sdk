package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.tag.Tag;

import java.util.*;

public class PacketDebugCommand extends MangoliseCommand {
    private static final Set<String> defaultIgnoredPackets = Set.of(
            "SystemChatPacket", "TimeUpdatePacket", "KeepAlivePacket", "ClientPlayerPositionPacket",
            "ClientPlayerRotationPacket", "ClientPlayerPositionAndRotationPacket", "PlayerInfoUpdatePacket",
            "ClientKeepAlivePacket", "EntityAttributesPacket", "EntityMetaDataPacket", "ClientEntityActionPacket",
            "ChunkDataPacket", "ChunkBatchStartPacket", "ChunkBatchFinishedPacket", "ClientChunkBatchReceivedPacket",
            "UnloadChunkPacket", "ClientSettingsPacket"
    );

    private static final Tag<Boolean> IS_LISTENING = Tag.Boolean("gamesdk_packet_is_listening").defaultValue(false);
    private static final Tag<Set<String>> IGNORED_PACKETS = Tag.<Set<String>>Transient("gamesdk_listen_ignored_packets").defaultValue(defaultIgnoredPackets);

    private final EventListener<PlayerPacketEvent> packetEventListener;
    private final EventListener<PlayerPacketOutEvent> packetOutEventListener;

    @Override
    protected String getPermission() {
        return "net.mangolise.packetdebug";
    }

    public PacketDebugCommand() {
        super("packetlisten");

        packetEventListener = net.minestom.server.event.EventListener.of(PlayerPacketEvent.class, e -> {
            if (e.getPlayer().getTag(IS_LISTENING)) {
                onPacket(e.getPlayer(), e.getPacket(), null, false);
            }
        });

        packetOutEventListener = EventListener.of(PlayerPacketOutEvent.class, e -> {
            if (e.getPlayer().getTag(IS_LISTENING)) {
                onPacket(e.getPlayer(), null, e.getPacket(), true);
            }
        });

        addPlayerSyntax(this::toggleListen);
        addPlayerSyntax(this::listPacketIgnores, ArgumentType.Literal("list"));
        addPlayerSyntax(this::setIgnored, ArgumentType.Word("ignore").from("ignore", "listen"), ArgumentType.StringArray("packets"));
    }

    private void listPacketIgnores(Player player, CommandContext context) {
        String packets = String.join(", ", player.getTag(IGNORED_PACKETS));
        player.sendMessage(ChatUtil.toComponent("&aPackets you are ignoring: &6" + packets));
    }

    private void onPacket(Player p, ClientPacket clientPacket, ServerPacket serverPacket, boolean out) {
        Set<String> ignoredPackets = p.getTag(IGNORED_PACKETS);

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

    private void toggleListen(Player player, CommandContext context) {
        if (player.getTag(IS_LISTENING)) {
            player.removeTag(IS_LISTENING);
            player.sendMessage("Stopped debugging packets");

            player.eventNode().addListener(packetEventListener);
            player.eventNode().addListener(packetOutEventListener);
        }
        else {
            player.setTag(IS_LISTENING, true);
            player.sendMessage("Started debugging packets");

            player.eventNode().removeListener(packetEventListener);
            player.eventNode().removeListener(packetOutEventListener);
        }
    }

    private void setIgnored(Player player, CommandContext context) {
        boolean ignore = context.get("ignore").equals("ignore");

        Set<String> ignoredPackets = player.getTag(IGNORED_PACKETS);
        if (!player.hasTag(IGNORED_PACKETS)) {
            ignoredPackets = new HashSet<>(ignoredPackets);
            player.setTag(IGNORED_PACKETS, ignoredPackets);
        }

        for (String packet : context.<String[]>get("packets")) {
            if (ignoredPackets.contains(packet) && ignore) {
                ignoredPackets.add(packet);
            } else if (!ignore) {
                ignoredPackets.remove(packet);
            }
        }
    }
}
