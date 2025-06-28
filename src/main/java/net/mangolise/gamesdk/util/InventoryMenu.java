package net.mangolise.gamesdk.util;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class InventoryMenu {
    private static final Tag<UUID> MENU_UUID_TAG = Tag.UUID("gamesdk_menu_uuid");
    private static final Map<AbstractInventory, InventoryMenu> inventoryMap = Collections.synchronizedMap(new WeakHashMap<>());

    private final UUID inventoryUuid;
    private final Inventory inventory;
    private final Map<UUID, MenuItem> menuItems;

    static {
        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, InventoryMenu::onClick);
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketEvent.class, InventoryMenu::onPacket);
    }

    public InventoryMenu(Inventory inventory) {
        menuItems = new HashMap<>();
        this.inventory = inventory;
        this.inventoryUuid = UUID.randomUUID();
        inventoryMap.put(inventory, this);
    }

    public InventoryMenu(InventoryType type, String title) {
        this(new Inventory(type, title));
    }

    public InventoryMenu(InventoryType type, Component title) {
        this(new Inventory(type, title));
    }

    public Inventory getInventory() {
        return inventory;
    }

    private MenuItem createMenuItem(ItemStack icon) {
        UUID uuid = UUID.randomUUID();
        MenuItem item = new MenuItem(icon.withTag(MENU_UUID_TAG, uuid), uuid);
        menuItems.put(uuid, item);

        return item;
    }

    /**
     * Creates a menu item and adds it to the first available slot
     * @param icon the icon of the item in the inventory
     * @return the menu item, which does nothing by default
     */
    public MenuItem addMenuItem(ItemStack icon) {
        MenuItem item = createMenuItem(icon);
        inventory.addItemStack(item.icon());

        return item;
    }

    /**
     * Creates a menu item and adds it to the first available slot
     * @param icon the icon of the item in the inventory
     * @param name the name of the item
     * @return the menu item, which does nothing by default
     */
    public MenuItem addMenuItem(Material icon, Component name) {
        return addMenuItem(ItemStack.of(icon).withCustomName(name));
    }

    /**
     * Creates a menu item and adds it to the first available slot
     * @param icon the icon of the item in the inventory
     * @param name the name of the item
     * @return the menu item, which does nothing by default
     */
    public MenuItem addMenuItem(Material icon, String name) {
        return addMenuItem(ItemStack.of(icon).withCustomName(Component.text(name)));
    }

    /**
     * Creates a menu item and adds it to a specified slot.
     * If the slot is taken by another menu item, then it will be overwritten
     * @param slot the slot to add the item to
     * @param icon the icon of the item in the inventory
     * @return the menu item, which does nothing by default
     */
    public MenuItem setMenuItem(int slot, ItemStack icon) {
        MenuItem item = createMenuItem(icon);
        inventory.setItemStack(slot, item.icon());

        return item;
    }

    /**
     * Creates a menu item and adds it to a specified slot.
     * If the slot is taken by another menu item, then it will be overwritten
     * @param slot the slot to add the item to
     * @param icon the icon of the item in the inventory
     * @param name the name of the item
     * @return the menu item, which does nothing by default
     */
    public MenuItem setMenuItem(int slot, Material icon, Component name) {
        return setMenuItem(slot, ItemStack.of(icon).withCustomName(name));
    }

    /**
     * Creates a menu item and adds it to a specified slot.
     * If the slot is taken by another menu item, then it will be overwritten
     * @param slot the slot to add the item to
     * @param icon the icon of the item in the inventory
     * @param name the name of the item
     * @return the menu item, which does nothing by default
     */
    public MenuItem setMenuItem(int slot, Material icon, String name) {
        return setMenuItem(slot, ItemStack.of(icon).withCustomName(Component.text(name)));
    }

    private static void onPacket(PlayerPacketEvent e) {
        if (!(e.getPacket() instanceof ClientClickWindowPacket packet)) {
            return;
        }

        AbstractInventory inventory = e.getPlayer().getOpenInventory();

        if (inventory == null || !inventoryMap.containsKey(inventory) ||
                packet.windowId() != inventory.getWindowId() ||
                packet.slot() < 0 || packet.slot() >= inventory.getSize()) {
            return;
        }

        InventoryMenu menu = inventoryMap.get(inventory);

        MenuItem item = menu.menuItems.get(inventory.getItemStack(packet.slot()).getTag(MENU_UUID_TAG));
        if (item == null) {
            return;
        }

        Player player = e.getPlayer();
        if (packet.clickType() == ClientClickWindowPacket.ClickType.QUICK_MOVE) {
            // Shift click

            if (packet.button() == 0) { // Shift Left click
                MenuItemClickEvent event = new MenuItemClickEvent(menu, player, item, MenuClickType.SHIFT_LEFT, -1);

                if (item.onShiftLeftClick != null) {
                    item.onShiftLeftClick.accept(event);
                } else if (item.onLeftClick != null) {
                    item.onLeftClick.accept(event);
                }
            }
            else { // Shift Right click
                MenuItemClickEvent event = new MenuItemClickEvent(menu, player, item, MenuClickType.SHIFT_RIGHT, -1);

                if (item.onShiftRightClick != null) {
                    item.onShiftRightClick.accept(event);
                } else if (item.onRightClick != null) {
                    item.onRightClick.accept(event);
                } else if (item.onLeftClick != null) {
                    item.onLeftClick.accept(event);
                }
            }
        }
        else if (packet.clickType() == ClientClickWindowPacket.ClickType.SWAP) {
            // Either hotbar number button or swap with offhand button
            // packet.button() is which hotbar slot it is swapped with

            if (packet.button() == 40) { // If it is swap with offhand
                if (item.onSwapClick != null) {
                    item.onSwapClick.accept(new MenuItemClickEvent(menu, player, item, MenuClickType.SWAP, -1));
                }
                return;
            }

            if (item.onHotbarClick != null) {
                // clamp the slot to within 0-8 so that the player cant send packets with a modified client that causes bugs
                item.onHotbarClick.accept(new MenuItemClickEvent(menu, player, item, MenuClickType.HOTBAR, Math.clamp(packet.button(), 0, 8)));
            }
        }
    }

    private static void onClick(InventoryPreClickEvent e) {
        // only continue if the player has this inventory menu open
        AbstractInventory inventory = e.getInventory();
        if (!inventoryMap.containsKey(inventory)) {
            return;
        }

        InventoryMenu menu = inventoryMap.get(inventory);

        e.setCancelled(true);

        UUID uuid = e.getClickedItem().getTag(MENU_UUID_TAG);
        if (uuid == null) {
            // should only happen if they use .getInventory() and add an item manually, which is fine to do
            return;
        }

        MenuItem menuItem = menu.menuItems.get(uuid);
        if (menuItem == null) {
            // this should not happen, unless the player manually adds a different menu item to this inventory
            return;
        }

        Click clickType = e.getClick();
        Player player = e.getPlayer();

        // not all cases are handled here, some just are not possible with a vanilla client in this situation
        // and others have to be handled in the packet because this event doesn't give all the information needed
        switch (clickType) {
            case Click.Left ignored -> {
                if (menuItem.onLeftClick != null) {
                    menuItem.onLeftClick.accept(new MenuItemClickEvent(menu, player, menuItem, MenuClickType.LEFT, -1));
                }
            }

            case Click.Right ignored -> {
                if (menuItem.onRightClick != null) {
                    menuItem.onRightClick.accept(new MenuItemClickEvent(menu, player, menuItem, MenuClickType.RIGHT, -1));
                } else if (menuItem.onLeftClick != null) {
                    menuItem.onLeftClick.accept(new MenuItemClickEvent(menu, player, menuItem, MenuClickType.RIGHT, -1));
                }
            }

            case Click.DropSlot ignored -> {
                if (menuItem.onDropClick != null) {
                    menuItem.onDropClick.accept(new MenuItemClickEvent(menu, player, menuItem, MenuClickType.DROP, -1));
                }
            }

            case Click.LeftShift ignored -> {
                // This is handled in packet listener because there is no way to identify if it was a right click
                // or a left click that did the shift click
            }

            case Click.HotbarSwap ignored -> {
                // This is handled in packet listener because there is no way to identify which slot was swapped with
            }

            default -> { /* Should not be possible, the others cant happen when you cancel the event */ }
        }
    }

    public static class MenuItem {
        private final ItemStack icon;
        private final UUID uuid;
        private @Nullable Consumer<MenuItemClickEvent> onLeftClick;
        private @Nullable Consumer<MenuItemClickEvent> onRightClick;
        private @Nullable Consumer<MenuItemClickEvent> onShiftLeftClick;
        private @Nullable Consumer<MenuItemClickEvent> onShiftRightClick;
        private @Nullable Consumer<MenuItemClickEvent> onDropClick;
        private @Nullable Consumer<MenuItemClickEvent> onSwapClick;
        private @Nullable Consumer<MenuItemClickEvent> onHotbarClick;


        protected MenuItem(ItemStack icon, UUID uuid) {
            this.icon = icon;
            this.uuid = uuid;
        }

        public ItemStack icon() {
            return icon;
        }

        public UUID uuid() {
            return uuid;
        }

        /**
         * Called when the player left-clicks on the menu item.
         * <p>
         * Will also be called on right-click if right-click isn't defined <br>
         * Will also be called on shift-left-click if shift-left-click isn't defined <br>
         * Will also be called on shift-right-click if shift-right-click and right-click are not defined
         * @param onLeftClick the consumer to call
         * @return this
         */
        public MenuItem onLeftClick(Consumer<MenuItemClickEvent> onLeftClick) {
            this.onLeftClick = onLeftClick;
            return this;
        }

        /**
         * Called when the player right-clicks on the menu item.
         * <p>
         * Will call left-click if not defined
         * <p>
         * Will also be called on shift-right-click if shift-right-click isn't defined
         * @param onRightClick the consumer to call
         * @return this
         */
        public MenuItem onRightClick(@Nullable Consumer<MenuItemClickEvent> onRightClick) {
            this.onRightClick = onRightClick;
            return this;
        }

        /**
         * Called when the player shift-clicks on the menu item with either left or right click.
         * <p>
         * Will call left-click/right-click if not defined depending on which button the player uses <br>
         * Will call left-click if this and right-click are not defined
         * @param onShiftClick the consumer to call
         * @return this
         */
        public MenuItem onShiftClick(@Nullable Consumer<MenuItemClickEvent> onShiftClick) {
            this.onShiftLeftClick = onShiftClick;
            this.onShiftRightClick = onShiftClick;
            return this;
        }

        /**
         * Called when the player shift-left-clicks on the menu item.
         * <p>
         * Will call left-click if not defined
         * @param onShiftLeftClick the consumer to call
         * @return this
         */
        public MenuItem onShiftLeftClick(@Nullable Consumer<MenuItemClickEvent> onShiftLeftClick) {
            this.onShiftLeftClick = onShiftLeftClick;
            return this;
        }

        /**
         * Called when the player shift-right-clicks on the menu item.
         * <p>
         * Will call right-click if not defined <br>
         * Will call left-click if this and right-click are not defined
         * @param onShiftRightClick the consumer to call
         * @return this
         */
        public MenuItem onShiftRightClick(@Nullable Consumer<MenuItemClickEvent> onShiftRightClick) {
            this.onShiftRightClick = onShiftRightClick;
            return this;
        }

        /**
         * Called when the player tries to drop a menu item.
         * @param onDropClick the consumer to call
         * @return this
         */
        public MenuItem onDropClick(@Nullable Consumer<MenuItemClickEvent> onDropClick) {
            this.onDropClick = onDropClick;
            return this;
        }

        /**
         * Called when the player tries to swap a menu item with their offhand.
         * @param onSwapClick the consumer to call
         * @return this
         */
        public MenuItem onSwapClick(@Nullable Consumer<MenuItemClickEvent> onSwapClick) {
            this.onSwapClick = onSwapClick;
            return this;
        }

        /**
         * Called when the player tries to move an item to their hotbar with a hotbar hotkey.
         * @param onHotbarClick the consumer to call
         * @return this
         */
        public MenuItem onHotbarClick(@Nullable Consumer<MenuItemClickEvent> onHotbarClick) {
            this.onHotbarClick = onHotbarClick;
            return this;
        }
    }

    public record MenuItemClickEvent(InventoryMenu menu, Player player, MenuItem clickedItem, MenuClickType clickType, int hotbarSlot) { }

    public enum MenuClickType {
        LEFT,
        RIGHT,
        SHIFT_LEFT,
        SHIFT_RIGHT,
        DROP,
        SWAP,
        HOTBAR
    }
}
