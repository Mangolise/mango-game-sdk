package net.mangolise.gamesdk.tablist;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class CustomTabList {
    private static final Tag<List<UUID>> LAST_ENTRIES = Tag.Transient("gamesdk.tablist.last_entries");

    private Component header = Component.text("");
    private Component footer = Component.text("");
    private final Set<Player> players = new HashSet<>();
    private final List<SendablePacket> packetsToIgnore = new ArrayList<>();
    private Function<Player, List<TabListEntry>> entriesProvider = player -> MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
            .map(p -> new TabListEntry(p.getUuid(), Component.text(p.getUsername()), p.getLatency(), p.getGameMode()))
            .toList();

    public void addPlayer(Player player) {
        players.add(player);

        // We need to remove any existing players that are in the tab list for this player
        // let's assume that every player in the server has a tab list entry
        List<UUID> playerUuids = MinecraftServer.getConnectionManager().getOnlinePlayers().stream().map(Entity::getUuid).toList();
        PlayerInfoRemovePacket removePacket = new PlayerInfoRemovePacket(playerUuids);
        player.sendPacket(removePacket);

        // Now we can add our entries to the tab list
        updateTabList(player);

        // Send updates to the player to disable visibility of entries whenever they are sent
        player.eventNode().addListener(PlayerPacketOutEvent.class, e -> {
            if (!(e.getPacket() instanceof PlayerInfoUpdatePacket packet)) {
                return;
            }

            // If the reference is already in the ignore list, skip processing
            if (packetsToIgnore.contains(packet)) {
                packetsToIgnore.remove(packet);
                return;
            }

            // Make sure none of the entries are visible in the tab list
            List<PlayerInfoUpdatePacket.Entry> newEntries = disableVisibilityForPacketEntries(packet);

            PlayerInfoUpdatePacket newPacket = new PlayerInfoUpdatePacket(EnumSet.of(PlayerInfoUpdatePacket.Action.UPDATE_LISTED), newEntries);
            packetsToIgnore.add(newPacket);
            player.sendPacket(newPacket);  // Send the modified packet to the player
        });
    }

    private static @NotNull List<PlayerInfoUpdatePacket.Entry> disableVisibilityForPacketEntries(PlayerInfoUpdatePacket packet) {
        List<PlayerInfoUpdatePacket.Entry> newEntries = new ArrayList<>();
        for (PlayerInfoUpdatePacket.Entry entry : packet.entries()) {
            newEntries.add(new PlayerInfoUpdatePacket.Entry(
                    entry.uuid(),
                    entry.username(),
                    entry.properties(),
                    false,
                    entry.latency(),
                    entry.gameMode(),
                    entry.displayName(),
                    entry.chatSession(),
                    entry.listOrder()));
        }
        return newEntries;
    }

    private void updateTabList(Player player) {
        PlayerListHeaderAndFooterPacket packet = new PlayerListHeaderAndFooterPacket(header, footer);
        player.sendPacket(packet);

        // We need to remove existing entries
        List<UUID> currentEntries = player.getTag(LAST_ENTRIES);
        if (currentEntries != null) {
            PlayerInfoRemovePacket removePacket = new PlayerInfoRemovePacket(currentEntries);
            player.sendPacket(removePacket);
        }

        List<PlayerInfoUpdatePacket.Entry> packetEntries = buildPacketEntryList(player);

        PlayerInfoUpdatePacket updatePacket = new PlayerInfoUpdatePacket(
                EnumSet.of(
                        PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                        PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                        PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                        PlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
                        PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER),
                packetEntries
        );
        packetsToIgnore.add(updatePacket);
        player.sendPacket(updatePacket);

        player.setTag(LAST_ENTRIES, new ArrayList<>(packetEntries.stream().map(PlayerInfoUpdatePacket.Entry::uuid).toList()));
    }

    private @NotNull List<PlayerInfoUpdatePacket.Entry> buildPacketEntryList(Player player) {
        List<TabListEntry> entries = entriesProvider.apply(player);

        List<PlayerInfoUpdatePacket.Entry> packetEntries = new ArrayList<>();
        int priority = entries.size();
        for (TabListEntry entry : entries) {
            packetEntries.add(new PlayerInfoUpdatePacket.Entry(
                    entry.uuid(),
                    "asd",
                    Collections.emptyList(), // No properties
                    true, // Visible
                    entry.latency(),
                    entry.gameMode(),
                    entry.text(), // No display name
                    null, // No chat session
                    priority-- // Default list order
            ));
        }
        return packetEntries;
    }

    public void update() {
        for (Player player : players) {
            updateTabList(player);
        }
    }

    public Component getFooter() {
        return footer;
    }

    public Component getHeader() {
        return header;
    }

    public void setFooter(Component footer) {
        this.footer = footer;
        update();
    }

    public void setHeader(Component header) {
        this.header = header;
        update();
    }

    public Function<Player, List<TabListEntry>> getEntriesProvider() {
        return entriesProvider;
    }

    public void setEntriesProvider(Function<Player, List<TabListEntry>> entriesProvider) {
        this.entriesProvider = entriesProvider;
        update();
    }
}
