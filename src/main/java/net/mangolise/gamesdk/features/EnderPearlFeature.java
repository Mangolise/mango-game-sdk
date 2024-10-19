package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnderPearlFeature implements Game.Feature<Game> {

    @Override
    public void setup(Context<Game> context) {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, this::useItem);
    }

    private void useItem(@NotNull PlayerUseItemEvent event) {
        if (!event.getItemStack().material().equals(Material.ENDER_PEARL)) {
            return;
        }

        Player player = event.getPlayer();
        player.setItemInHand(event.getHand(), event.getItemStack().consume(1));
        player.refreshItemUse(event.getHand(), 120);

        // We have an ender pearl
        Entity pearl = new EntityProjectile(player, EntityType.ENDER_PEARL);

        pearl.setInstance(player.getInstance()).thenRun(() -> {
            pearl.teleport(player.getPosition().add(0, player.getEyeHeight(), 0));
            pearl.setVelocity(player.getPosition().direction().mul(32));
        });

        pearl.eventNode().addListener(ProjectileCollideWithBlockEvent.class, collideEvent ->
                pearlLand(collideEvent.getEntity(), player));

        pearl.eventNode().addListener(ProjectileCollideWithEntityEvent.class, collideEvent ->
                pearlLand(collideEvent.getEntity(), player));
    }

    private Pos getSafeTpPos(Entity pearl) {
        Pos actual = pearl.getPosition().sub(pearl.getPosition().direction());
        if (pearl.getInstance().getBlock(actual).isAir()) {
            return actual;
        }

        List<Pos> alternatives = List.of(
                // Do adjacent first
                actual.add(0, 0, 1),
                actual.add(1, 0, 0),
                actual.add(0, 0, -1),
                actual.add(-1, 0, 0),

                // Then up down
                actual.add(0, -1, 0),
                actual.add(0, 1, 0)
        );

        for (Pos pos : alternatives) {
            if (pearl.getInstance().getBlock(pos).isAir()) {
                return pos;
            }
        }

        return actual;
    }

    private void pearlLand(Entity pearl, Player thrower) {
        if (thrower == null || !thrower.isOnline()) {
            pearl.remove();
            return;
        }

        thrower.teleport(getSafeTpPos(pearl));
        pearl.remove();
    }
}
