package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.features.crafting.CraftingRecipe;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CraftingFeature implements Game.Feature<Game> {
    private final List<CraftingRecipe> recipes;
    public static final List<Integer> PINV_CRAFTING_SLOTS = List.of(37, 38, 39, 40);
    public static final int PINV_RESULT_SLOT = 36;
    public static final int CINV_RESULT_SLOT = 0;

    public CraftingFeature(List<CraftingRecipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    public void setup(Context<Game> context) {
        context.eventNode().addListener(InventoryPreClickEvent.class, this::invPreClick);
        context.eventNode().addListener(InventoryClickEvent.class, this::invClick);
        context.eventNode().addListener(PlayerBlockInteractEvent.class, this::blockInteract);
    }

    private void blockInteract(@NotNull PlayerBlockInteractEvent event) {
        if (event.getBlock().compare(Block.CRAFTING_TABLE)) {
            event.getPlayer().openInventory(new Inventory(InventoryType.CRAFTING, "Crafting"));
        }
    }

    private void invClick(@NotNull InventoryClickEvent event) {
        if (event.getPlayer().getOpenInventory() == null) {  // Player inventory
            playerInvClick(event);
            return;
        }

        if (event.getPlayer().getOpenInventory() instanceof Inventory inventory && inventory.getInventoryType() == InventoryType.CRAFTING) {
            craftingInvClick(event);
            return;
        }
    }

    private List<Material> getPInvSlots(PlayerInventory inventory) {
        return List.of(
                inventory.getItemStack(PINV_CRAFTING_SLOTS.getFirst()).material(), inventory.getItemStack(PINV_CRAFTING_SLOTS.get(1)).material(), Material.AIR,
                inventory.getItemStack(PINV_CRAFTING_SLOTS.get(2)).material(), inventory.getItemStack(PINV_CRAFTING_SLOTS.get(3)).material(), Material.AIR,
                Material.AIR, Material.AIR, Material.AIR
        );
    }

    private List<Material> getCInvSlots(AbstractInventory inventory) {
        List<Material> items = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            items.add(inventory.getItemStack(i).material());
        }
        return List.copyOf(items);
    }

    private List<ItemStack> getCInvSlotItems(AbstractInventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            items.add(inventory.getItemStack(i));
        }
        return List.copyOf(items);
    }

    // TODO: After shift clicking result slot doesn't seem to update correctly
    private CraftingRecipe updateTargetRecipe(List<Material> slots, AbstractInventory inventory, int resultSlot) {
        for (CraftingRecipe recipe : recipes) {
            if (!recipe.canCraft(slots)) continue;
            inventory.setItemStack(resultSlot, recipe.getCraftingResult());
            return recipe;
        }

        inventory.setItemStack(resultSlot, ItemStack.AIR);
        return null;
    }

    private void playerInvClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();

//        player.sendMessage("Click slot type(" + event.getSlot() + "): " + inventory.getItemStack(event.getSlot()).material().name());
        List<Material> slots = getPInvSlots(inventory);

        CraftingRecipe targetRecipe = updateTargetRecipe(slots, inventory, PINV_RESULT_SLOT);
        if (targetRecipe == null) {
            return;
        }

        if (inventory.getItemStack(event.getSlot()).isAir())  {
            return;
        }


        if (!event.getClickedItem().isAir() && event.getClickedItem().isSimilar(inventory.getItemStack(PINV_RESULT_SLOT))) {
            List<ItemStack> existingSlots = new ArrayList<>(List.of(
                    inventory.getItemStack(PINV_CRAFTING_SLOTS.getFirst()), inventory.getItemStack(PINV_CRAFTING_SLOTS.get(1)), ItemStack.AIR,
                    inventory.getItemStack(PINV_CRAFTING_SLOTS.get(2)), inventory.getItemStack(PINV_CRAFTING_SLOTS.get(3)), ItemStack.AIR,
                    ItemStack.AIR, ItemStack.AIR, ItemStack.AIR
            ));

            if (event.getClickType() == ClickType.SHIFT_CLICK) {
                boolean firstTime = true;
                while (targetRecipe.canCraft(slots)) {
                    if (!firstTime && !inventory.addItemStack(targetRecipe.getCraftingResult())) {
                        break;
                    }
                    firstTime = false;

                    removeAndUpdateItemPInv(inventory, targetRecipe, existingSlots);
                    slots = getPInvSlots(inventory);
                }
            } else {
                removeAndUpdateItemPInv(inventory, targetRecipe, existingSlots);
            }
        }

        slots = getPInvSlots(inventory);
        updateTargetRecipe(slots, inventory, PINV_RESULT_SLOT);
    }

    private void removeAndUpdateItemPInv(PlayerInventory inventory, CraftingRecipe targetRecipe, List<ItemStack> existingSlots) {
        targetRecipe.removeItems(existingSlots);

        inventory.setItemStack(PINV_CRAFTING_SLOTS.getFirst(), existingSlots.getFirst());
        inventory.setItemStack(PINV_CRAFTING_SLOTS.get(1), existingSlots.get(1));
        inventory.setItemStack(PINV_CRAFTING_SLOTS.get(2), existingSlots.get(3));
        inventory.setItemStack(PINV_CRAFTING_SLOTS.get(3), existingSlots.get(4));
    }

    private void craftingInvClick(@NotNull InventoryClickEvent event) {
        // event.getInventory() will be the player inventory (null) if they shift clicked or hotkeyed into their inventory
        // otherwise it will be the crafting grid inventory. The player's open inventory will always be the crafting grid
        // at this point because we checked it in the event handler
        AbstractInventory inventory = event.getInventory() == null ? event.getPlayer().getInventory() : event.getInventory();
        AbstractInventory craftingInv = event.getPlayer().getOpenInventory();
        assert craftingInv != null;  // We checked it in the method that calls this

        List<Material> slots = getCInvSlots(craftingInv);

        CraftingRecipe targetRecipe = updateTargetRecipe(slots, craftingInv, CINV_RESULT_SLOT);
        if (targetRecipe == null) {
            return;
        }

        if (inventory.getItemStack(event.getSlot()).isAir())  {
            return;
        }

        if (!event.getClickedItem().isAir() && event.getClickedItem().isSimilar(craftingInv.getItemStack(CINV_RESULT_SLOT))) {
            final List<ItemStack> existingSlots = new ArrayList<>(getCInvSlotItems(craftingInv));

            if (event.getClickType() == ClickType.SHIFT_CLICK) {  // Inventory is the player inv (They clicked in the target loc for shift click)
                boolean firstTime = true;
                while (targetRecipe.canCraft(slots)) {
                    if (!firstTime && !inventory.addItemStack(targetRecipe.getCraftingResult())) {
                        break;
                    }
                    firstTime = false;

                    targetRecipe.removeItems(existingSlots);
                    for (int i = 0; i < 9; i++) {
                        craftingInv.setItemStack(i + 1, existingSlots.get(i));
                    }
                    slots = getCInvSlots(craftingInv);
                }
            } else {
                targetRecipe.removeItems(existingSlots);

                for (int i = 0; i < 9; i++) {
                    inventory.setItemStack(i + 1, existingSlots.get(i));
                }
            }
        }

        slots = getCInvSlots(craftingInv);
        updateTargetRecipe(slots, craftingInv, CINV_RESULT_SLOT);
    }

    private void invPreClick(@NotNull InventoryPreClickEvent event) {
        int resultSlot = event.getInventory() == null ? PINV_RESULT_SLOT : CINV_RESULT_SLOT;
        ItemStack clickedItem = event.getPlayer().getInventory().getCursorItem();
        if (!(event.getClick() instanceof Click.LeftShift && event.getClick() instanceof Click.RightShift) &&
                (!clickedItem.isAir() && event.getSlot() == resultSlot && !clickedItem.material().equals(event.getClickedItem().material()))) {
            event.setCancelled(true);
        }
    }
}
