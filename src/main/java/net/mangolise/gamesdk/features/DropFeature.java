package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.item.ItemDropEvent;
import org.jetbrains.annotations.NotNull;

public class DropFeature implements Game.Feature<Game> {
    private static final double THROW_SPEED = 5;

    @Override
    public void setup(Context<Game> context) {
        MinecraftServer.getGlobalEventHandler().addListener(ItemDropEvent.class, this::itemDrop);
    }

    private void itemDrop(@NotNull ItemDropEvent event) {
        Entity item = GameSdkUtils.dropItemNaturally(event.getInstance(), event.getPlayer().getPosition().add(0, 1.3, 0), event.getItemStack());
        item.setVelocity(event.getPlayer().getPosition().direction().mul(THROW_SPEED));
    }
}
