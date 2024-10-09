package net.mangolise.gamesdk.features.crafting;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShapedRecipe implements CraftingRecipe {
    public final List<Set<Material>> shape;
    public final int width;
    public final int height;
    public final ItemStack result;

    public ShapedRecipe(List<Set<Material>> shape, int width, int height, ItemStack result) {
        if (shape.size() != width * height) {
            throw new IllegalArgumentException("Shape size must be width * height");
        }

        this.shape = shape;
        this.width = width;
        this.height = height;
        this.result = result;
    }

    @Override
    public boolean canCraft(final List<Material> slots) {
        if (slots.stream().allMatch(m -> m.equals(Material.AIR))) {
            return false;
        }

        List<Material> modSlots = new ArrayList<>(slots);
        int dimension = (int) Math.sqrt(modSlots.size());   // Get the side length of the square
        int w = dimension;
        int h = dimension;

        // Remove empty rows
        {
            int row = 0;
            rowsIter: while (row < h) {
                for (int col = 0; col < w; col++) {
                    int slot = (row * w) + col;
                    if (!modSlots.get(slot).equals(Material.AIR)) {
                        row++;
                        continue rowsIter;
                    }
                }

                // Remove row
                for (int i = 0; i < w; i++) {
                    modSlots.remove(row * w);
                }
                h--;
            }
        }

        // Remove empty column
        {
            int col = 0;
            colIter: while (col < w) {
                for (int row = 0; row < h; row++) {
                    int slot = (row * w) + col;
                    if (!modSlots.get(slot).equals(Material.AIR)) {
                        col++;
                        continue colIter;
                    }
                }

                // Remove column
                for (int i = 0; i < h; i++) {
                    try {
                        modSlots.remove((i * w) + col - i);
                    } catch (RuntimeException e) {
                        throw new RuntimeException(e);
                    }
                }
                w--;
            }
        }

        if (modSlots.size() != shape.size()) {
            return false;
        }

        for (int i = 0; i < modSlots.size(); i++) {
            if (!shape.get(i).contains(modSlots.get(i))) {
                return false;
            }
        }

        return true;
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
