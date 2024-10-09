package net.mangolise.gamesdk.features.crafting;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UnShapedRecipe implements CraftingRecipe {
    public final List<Set<Material>> items;
    public final ItemStack result;

    public UnShapedRecipe(List<Set<Material>> items, ItemStack result) {
        this.items = items;
        this.result = result;
    }

    @Override
    public boolean canCraft(List<Material> slots) {
        List<Set<Material>> needed = new ArrayList<>(items);
        List<Material> modSlots = new ArrayList<>(slots);

        items: while (!modSlots.isEmpty()) {
            Material current = modSlots.getFirst();
            if (current.equals(Material.AIR)) {
                modSlots.removeFirst();
                continue;
            }

            for (int i = 0; i < needed.size(); i++) {
                if (needed.get(i).contains(current)) {
                    needed.remove(i);
                    modSlots.removeFirst();
                    continue items;
                }
            }

            // It's not needed, therefore invalid
            return false;
        }

        return needed.isEmpty();  // If we don't need anything else then we did it
    }

    @Override
    public void removeItems(List<ItemStack> slots) {
        slots.replaceAll(itemStack -> {
            if (itemStack.isAir() || itemStack.amount() == 1) {
                return ItemStack.AIR;
            }

            return itemStack.consume(1);
        });
    }

    @Override
    public ItemStack getCraftingResult() {
        return result;
    }
}
