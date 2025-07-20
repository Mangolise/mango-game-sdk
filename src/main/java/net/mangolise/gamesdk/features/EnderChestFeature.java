package net.mangolise.gamesdk.features;

import net.kyori.adventure.sound.Sound;
import net.mangolise.gamesdk.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EnderChestFeature implements Game.Feature<Game> {
    // Player tags
    private static final Tag<Inventory> ENDER_CHEST_TAG = Tag.Transient("gamesdk_ender_chest_inventory");

    // Inventory
    private static final Tag<Boolean> IS_ENDER_CHEST = Tag.Boolean("gamesdk_ender_chest").defaultValue(false);
    private static final Tag<UUID> INSTANCE_UUID = Tag.UUID("gamesdk_ender_chest_instance_uuid");
    private static final Tag<Vec> ENDER_CHEST_POSITION = Tag.Structure("gamesdk_ender_chest_position", Vec.class);

    // Block
    private static final Map<ViewMapKey, Integer> viewCountMap = new HashMap<>();

    private static DefaultEnderChestSuppler supplier;

    public EnderChestFeature(DefaultEnderChestSuppler supplier) {
        EnderChestFeature.supplier = supplier;
    }

    public EnderChestFeature() {
        this(p -> new Inventory(InventoryType.CHEST_3_ROW, "Ender Chest"));
    }

    @Override
    public void setup(Context<Game> context) {
        context.eventNode().addListener(InventoryCloseEvent.class, this::onInventoryClose);
        MinecraftServer.getSchedulerManager().scheduleTask(this::every5Ticks, TaskSchedule.millis(250), TaskSchedule.millis(250));
    }

    /**
     * Opens the ender chest for a player
     *
     * @param player the player who is opening the inventory
     * @see #open(Player, Instance, Point, boolean)
     */
    public static void open(Player player) {
        Inventory inv = getInventory(player);
        inv.removeTag(ENDER_CHEST_POSITION);
        player.openInventory(inv);
    }

    /**
     * Opens the ender chest for a player and plays an animation in the block. <br>
     * if {@code global} is true, this animation will play for all players in the instance,
     * otherwise it will only play for the player that opened this inventory.
     *
     * @param player the player who is opening the inventory
     * @param instance the instance the ender chest block is in
     * @param blockPos the position of the ender chest block
     * @param global whether the animation should be played for all players or just the player who opened it
     */
    public static void open(Player player, Instance instance, Point blockPos, boolean global) {
        Inventory inv = getInventory(player);
        player.openInventory(inv);

        inv.setTag(ENDER_CHEST_POSITION, Vec.fromPoint(blockPos));

        // Animation
        if (global) {
            // Animation that is for everyone in the instance
            inv.setTag(INSTANCE_UUID, instance.getUuid());
            int viewCount = viewCountMap.compute(new ViewMapKey(instance.getUuid(), blockPos), (k, v) -> v == null ? 1 : v + 1);

            Chunk chunk = instance.getChunkAt(blockPos);
            if (chunk == null || !chunk.isLoaded()) {
                return;
            }

            chunk.sendPacketToViewers(new BlockActionPacket(blockPos, (byte) 1, (byte) viewCount, Block.ENDER_CHEST));

            if (viewCount == 1) {
                instance.playSound(getOpenCloseSound(SoundEvent.BLOCK_ENDER_CHEST_OPEN), blockPos);
            }
        } else {
            inv.removeTag(INSTANCE_UUID);
            player.sendPacket(new BlockActionPacket(blockPos, (byte) 1, (byte) 1, Block.ENDER_CHEST));
            player.playSound(getOpenCloseSound(SoundEvent.BLOCK_ENDER_CHEST_OPEN), blockPos);
        }
    }

    /**
     * @param player the player whose inventory we need
     * @return the ender chest's inventory
     */
    public static Inventory getInventory(Player player) {
        Inventory inv = player.getTag(ENDER_CHEST_TAG);
        if (inv == null) {
            inv = supplier.get(player);
            inv.setTag(IS_ENDER_CHEST, true);
            player.setTag(ENDER_CHEST_TAG, inv);
        }

        return inv;
    }

    /**
     * Sets the ender chest inventory for a player
     *
     * @param player the player
     * @param inventory the inventory to become the ender chest inventory
     */
    public static void setInventory(Player player, Inventory inventory) {
        Inventory oldInv = player.getTag(ENDER_CHEST_TAG);
        if (oldInv != null) {
            oldInv.removeTag(IS_ENDER_CHEST);
        }

        inventory.setTag(IS_ENDER_CHEST, true);
        player.setTag(ENDER_CHEST_TAG, inventory);
    }

    private void every5Ticks() {
        List<ViewMapKey> removeKeys = new ArrayList<>(0);
        viewCountMap.forEach((key, viewCount) -> {
            Instance instance = MinecraftServer.getInstanceManager().getInstance(key.instanceUuid());
            if (instance == null) {
                removeKeys.add(key);
                return;
            }

            Chunk chunk = instance.getChunkAt(key.pos());
            if (chunk == null || !chunk.isLoaded()) {
                return;
            }

            chunk.sendPacketToViewers(new BlockActionPacket(key.pos(), (byte) 1, viewCount.byteValue(), Block.ENDER_CHEST));
        });

        removeKeys.forEach(viewCountMap::remove);
    }

    private void onInventoryClose(InventoryCloseEvent e) {
        AbstractInventory inv = e.getInventory();
        if (!inv.getTag(IS_ENDER_CHEST)) {
            return;
        }

        Vec blockPos = inv.getTag(ENDER_CHEST_POSITION);
        EventDispatcher.call(new EnderChestCloseEvent(e.getPlayer(), inv, blockPos));

        if (blockPos == null) {
            return;
        }

        if (inv.hasTag(INSTANCE_UUID)) {
            // Animation that is for everyone in the instance
            UUID instanceUuid = inv.getTag(INSTANCE_UUID);
            Instance instance = MinecraftServer.getInstanceManager().getInstance(instanceUuid);
            if (instance == null) {
                List<ViewMapKey> removingKeys = new ArrayList<>();
                viewCountMap.forEach((k, v) -> {
                    if (k.instanceUuid.equals(instanceUuid)) {
                        removingKeys.add(k);
                    }
                });

                removingKeys.forEach(viewCountMap::remove);
                return;
            }

            Integer viewCount = viewCountMap.compute(new ViewMapKey(inv.getTag(INSTANCE_UUID), inv.getTag(ENDER_CHEST_POSITION)),
                    (k, v) -> (v == null || v <= 1) ? null : v - 1);

            if (viewCount == null) {
                viewCount = 0;
            }

            Chunk chunk = instance.getChunkAt(blockPos);
            if (chunk == null || !chunk.isLoaded()) {
                return;
            }

            chunk.sendPacketToViewers(new BlockActionPacket(blockPos, (byte) 1, viewCount.byteValue(), Block.ENDER_CHEST));

            if (viewCount == 0) {
                instance.playSound(getOpenCloseSound(SoundEvent.BLOCK_ENDER_CHEST_CLOSE), blockPos);
            }
        } else {
            e.getPlayer().sendPacket(new BlockActionPacket(blockPos, (byte) 1, (byte) 0, Block.ENDER_CHEST));
            e.getPlayer().playSound(getOpenCloseSound(SoundEvent.BLOCK_ENDER_CHEST_CLOSE), blockPos);
        }
    }

    private static Sound getOpenCloseSound(SoundEvent soundEvent) {
        return Sound.sound(soundEvent, Sound.Source.BLOCK, 0.5f, ThreadLocalRandom.current().nextFloat(0.9f, 1.0f));
    }

    private record ViewMapKey(UUID instanceUuid, BlockVec pos) {
        public ViewMapKey(UUID instanceUuid, Point pos) {
            this(instanceUuid, new BlockVec(pos));
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ViewMapKey(UUID uuid, BlockVec pos1)) {
                return instanceUuid.equals(uuid) && pos.equals(pos1);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return instanceUuid.hashCode() ^ pos.hashCode();
        }
    }

    @FunctionalInterface
    public interface DefaultEnderChestSuppler {
        Inventory get(Player player);
    }

    public static class EnderChestCloseEvent implements PlayerEvent {
        private final Player player;
        private final AbstractInventory inventory;
        private final @Nullable Point enderChestPosition;

        public EnderChestCloseEvent(Player player, AbstractInventory inventory, @Nullable Point enderChestPosition) {
            this.player = player;
            this.inventory = inventory;
            this.enderChestPosition = enderChestPosition;
        }

        @Override
        public @NotNull Player getPlayer() {
            return player;
        }

        /**
         * @return the ender chest's inventory
         */
        public AbstractInventory getInventory() {
            return inventory;
        }

        /**
         * @return the ender chest's position, null if it was opened without a source block
         */
        public @Nullable Point getEnderChestPosition() {
            return enderChestPosition;
        }
    }
}
