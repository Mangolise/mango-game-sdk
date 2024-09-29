package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ItemPickupFeature implements Game.Feature<Game> {
    private static final Tag<Long> ITEM_AGE_TAG = Tag.Long("dropped_item_birth");
    private static final int WAIT_BEFORE_PICKUP = 700;  // ms
    private static final float PICKUP_RANGE = 1.9f;  // ms

    @Override
    public void setup(Context<Game> context) {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerTickEvent.class, this::checkPickup);
        MinecraftServer.getGlobalEventHandler().addListener(EntitySpawnEvent.class, this::entitySpawn);
    }

    private long getAge(Entity entity) {
        return System.currentTimeMillis() - entity.getTag(ITEM_AGE_TAG);
    }

    private void entitySpawn(@NotNull EntitySpawnEvent event) {
        event.getEntity().setTag(ITEM_AGE_TAG, System.currentTimeMillis());
    }

    private void checkPickup(PlayerTickEvent event) {
        Player player = event.getPlayer();
        Collection<Entity> entities = player.getInstance().getNearbyEntities(player.getPosition(), PICKUP_RANGE);
        for (Entity entity : entities) {
            if (!entity.getEntityType().equals(EntityType.ITEM)) {
                continue;
            }

            if (getAge(entity) < WAIT_BEFORE_PICKUP) {  // Otherwise you can't drop
                continue;
            }

            ItemEntityMeta meta = (ItemEntityMeta) entity.getEntityMeta();
            ItemStack itemStack = meta.getItem();

            if (player.getInventory().addItemStack(itemStack)) {
                entity.sendPacketToViewers(new CollectItemPacket(entity.getEntityId(), player.getEntityId(), itemStack.amount()));
                entity.remove();
            }
        }
    }
}
