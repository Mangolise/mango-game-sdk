package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.item.ItemStack;

public class ItemPickupFeature implements Game.Feature<Game> {
    @Override
    public void setup(Context<Game> context) {
        context.eventNode().addListener(PickupItemEvent.class, this::onPickup);
    }

    private void onPickup(PickupItemEvent event) {
        if (!(event.getLivingEntity() instanceof Player player)) {
            return;
        }

        if (player.isDead() || player.getGameMode().equals(GameMode.SPECTATOR)) {
            event.setCancelled(true);
            return;
        }

        ItemStack newStack = player.getInventory().addItemStack(event.getItemStack(), TransactionOption.ALL);

        if (!newStack.isAir()) {
            event.getItemEntity().setItemStack(newStack);
            event.setCancelled(true);
        }
    }
}
