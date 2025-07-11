package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.inventory.*;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.type.VillagerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class VillagerTradeFeature implements Game.Feature<Game> {
    private static final int INGREDIENT_SLOT_0 = 0;
    private static final int INGREDIENT_SLOT_1 = 1;
    private static final int RESULT_SLOT = 2;

    private static final Tag<Integer> SELECTED_TRADE = Tag.Integer("gen_selected_trade");

    /**
     * Checks if itemStacks are the same and amounts are equal to or greater than.
     */
    private static boolean checkAmounts(ItemStack containerItem, ItemStack requiredItem) {
        return containerItem.isSimilar(requiredItem) && containerItem.amount() >= requiredItem.amount();
    }

    /**
     * Searches for itemStacks in multiple inventories, if it finds them remove them.
     */
    private static List<ItemStack> tryTakeItemStacks(List<AbstractInventory> inventories, List<ItemStack> itemStacks) {
        List<ItemStack> outputs = new ArrayList<>(itemStacks.size());

        for (ItemStack requiredItem : itemStacks) {
            int count = 0;

            https://inventory.com
            for (AbstractInventory inventory : inventories) {
                for (int i = 0; i < inventory.getSize(); i++) {
                    ItemStack item = inventory.getItemStack(i);
                    if (item.isSimilar(requiredItem)) {
                        if (count < requiredItem.maxStackSize()) {
                            inventory.setItemStack(i, ItemStack.AIR);
                            count += item.amount();
                        } else {
                            inventory.setItemStack(i, item.withAmount(item.amount() - (requiredItem.maxStackSize() - count)));
                            count = requiredItem.maxStackSize();
                            break https;
                        }
                    }
                }
            }

            outputs.add(requiredItem.withAmount(count));
        }

        return outputs;
    }

    private static @NotNull ItemStack itemCostToItemStack(TradeListPacket.ItemCost itemCost) {
        if (itemCost == null) {
            return ItemStack.AIR;
        }

        ItemStack.Builder item = ItemStack.builder(itemCost.material());
        item.amount(itemCost.amount());
        for (DataComponent.Value value : itemCost.components().entrySet()) {
            item.set((DataComponent<Object>) value.component(), value.value());
        }

        return item.build();
    }

    private record ReversableTrade(TradeListPacket.Trade trade, boolean reversed) { }

    private static List<ReversableTrade> getCurrentTrades(Player player, VillagerInventory inv) {
        if (player.hasTag(SELECTED_TRADE)) {
            return getValidTrade(inv, inv.getTrades().get(player.getTag(SELECTED_TRADE))).toList();
        }

        return inv.getTrades().stream().flatMap(trade -> getValidTrade(inv, trade)).toList();
    }

    private static Stream<ReversableTrade> getValidTrade(VillagerInventory inv, TradeListPacket.Trade trade) {
        if (trade.inputItem2() == null) {
            if (checkAmounts(inv.getItemStack(INGREDIENT_SLOT_0), itemCostToItemStack(trade.inputItem1())) && inv.getItemStack(INGREDIENT_SLOT_1).isAir()) {
                return Stream.of(new ReversableTrade(trade, false));
            } else if (checkAmounts(inv.getItemStack(INGREDIENT_SLOT_1), itemCostToItemStack(trade.inputItem1())) && inv.getItemStack(INGREDIENT_SLOT_0).isAir()) {
                return Stream.of(new ReversableTrade(trade, true));
            }

            return Stream.empty();
        }

        if (checkAmounts(inv.getItemStack(INGREDIENT_SLOT_0), itemCostToItemStack(trade.inputItem1())) && checkAmounts(inv.getItemStack(INGREDIENT_SLOT_1), itemCostToItemStack(trade.inputItem2()))) {
            return Stream.of(new ReversableTrade(trade, false));
        } else if (checkAmounts(inv.getItemStack(INGREDIENT_SLOT_1), itemCostToItemStack(trade.inputItem1())) && checkAmounts(inv.getItemStack(INGREDIENT_SLOT_0), itemCostToItemStack(trade.inputItem2()))) {
            return Stream.of(new ReversableTrade(trade, true));
        }

        return Stream.empty();
    }

    @Override
    public void setup(Context<Game> context) {
        context.eventNode().addListener(InventoryPreClickEvent.class, e -> {
            if (e.getPlayer().getInventory().equals(e.getInventory()) && e.getPlayer().getOpenInventory() instanceof VillagerInventory && (e.getClick() instanceof Click.LeftShift || e.getClick() instanceof Click.RightShift)) {
                e.setCancelled(true);
            }

            if (!(e.getInventory() instanceof VillagerInventory inv)) {
                return;
            }

            List<ReversableTrade> trades = getCurrentTrades(e.getPlayer(), inv);
            if (trades.size() != 1) {
                inv.setItemStack(RESULT_SLOT, ItemStack.AIR);
                return;
            }

            ReversableTrade trade = trades.getFirst();
            inv.setItemStack(RESULT_SLOT, trade.trade.result());

            if (e.getClick().slot() == RESULT_SLOT) {
                ItemStack resultItem = inv.getItemStack(RESULT_SLOT);
                ItemStack cursorItem = e.getPlayer().getInventory().getCursorItem();

                boolean sameType = resultItem.isSimilar(cursorItem);

                if (resultItem.isAir() || !cursorItem.isAir() && !sameType) {
                    e.setCancelled(true);
                    return;
                }

                if (sameType) {
                    if (resultItem.amount() + cursorItem.amount() > resultItem.material().maxStackSize()) {
                        e.setCancelled(true);
                        return;
                    }

                    inv.takeItemStack(resultItem, TransactionOption.ALL_OR_NOTHING);

                    MinecraftServer.getSchedulerManager().scheduleEndOfTick(() -> {
                        e.getPlayer().getInventory().setCursorItem(resultItem.withAmount(resultItem.amount() + cursorItem.amount()));
                    });
                }

                MinecraftServer.getSchedulerManager().scheduleEndOfTick(() -> {
                    if (trade.reversed) {
                        inv.setItemStack(INGREDIENT_SLOT_1, inv.getItemStack(INGREDIENT_SLOT_1).consume(trade.trade.inputItem1().amount()));

                        if (trade.trade.inputItem2() != null) {
                            inv.setItemStack(INGREDIENT_SLOT_0, inv.getItemStack(INGREDIENT_SLOT_0).consume(trade.trade.inputItem2().amount()));
                        }
                    } else {
                        inv.setItemStack(INGREDIENT_SLOT_0, inv.getItemStack(INGREDIENT_SLOT_0).consume(trade.trade.inputItem1().amount()));

                        if (trade.trade.inputItem2() != null) {
                            inv.setItemStack(INGREDIENT_SLOT_1, inv.getItemStack(INGREDIENT_SLOT_1).consume(trade.trade.inputItem2().amount()));
                        }
                    }
                });
            }
        });

        // This is needed for the ClientSelectTradePacket
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSelectTradePacket.class, (packet, player) -> {
            if (!(player.getOpenInventory() instanceof VillagerInventory inv)) {
                return;
            }

            List<TradeListPacket.Trade> currentOffers = inv.getTrades();
            if (currentOffers == null || packet.selectedSlot() >= currentOffers.size()) {
                return;
            }

            player.setTag(SELECTED_TRADE, packet.selectedSlot());

            TradeListPacket.Trade selectedOffer = currentOffers.get(packet.selectedSlot());
            PlayerInventory playerInventory = player.getInventory();

            ItemStack itemCost1 = itemCostToItemStack(selectedOffer.inputItem1());
            ItemStack itemCost2 = itemCostToItemStack(selectedOffer.inputItem2());

            List<ItemStack> items = tryTakeItemStacks(List.of(inv, playerInventory), List.of(itemCost1, itemCost2));
            inv.setItemStack(INGREDIENT_SLOT_0, items.getFirst());
            inv.setItemStack(INGREDIENT_SLOT_1, items.get(1));
        });

        context.eventNode().addListener(InventoryCloseEvent.class, e -> {
            e.getPlayer().removeTag(SELECTED_TRADE);
        });
    }
}

