package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.ItemDropEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ItemDropFeature implements Game.Feature<Game> {
    @Override
    public void setup(Context<Game> context) {
        MinecraftServer.getGlobalEventHandler().addListener(ItemDropEvent.class, this::itemDrop);
    }

    private void itemDrop(@NotNull ItemDropEvent event) {
        // Schedule end of tick so that it runs after every other event handler
        MinecraftServer.getSchedulerManager().scheduleEndOfTick(() -> {
            if (event.isCancelled()) {
                return;
            }

            Player player = event.getPlayer();
            Pos pos = player.getPosition();

            ItemEntity item = GameSdkUtils.dropItem(event.getInstance(), pos.add(0, player.getEyeHeight() - 0.3, 0), event.getItemStack());
            item.setPickupDelay(Duration.ofMillis(2000));

            // this aerodynamics causes less snapping
            item.setAerodynamics(new Aerodynamics(0.04, 0.95, 0.96));

            Random random = ThreadLocalRandom.current();

            // Copied from minecraft decompiled source code, with small changes to make it work with minestom
            // .mul(20) is because minecraft velocity is in blocks/tick whereas minestom is in blocks/second
            float g = (float) Math.sin(pos.pitch() * 0.017453292f);
            float h = (float) Math.cos(pos.pitch() * 0.017453292f);
            float i = (float) Math.sin(pos.yaw() * 0.017453292f);
            float j = (float) Math.cos(pos.yaw() * 0.017453292f);
            float k = random.nextFloat() * 6.2831855f;
            float l = 0.02F * random.nextFloat();
            item.setVelocity(new Vec((double)(-i * h * 0.3F) + Math.cos(k) * (double)l,
                    -g * 0.3F + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F,
                    (double)(j * h * 0.3F) + Math.sin(k) * (double)l).mul(20));
        });
    }
}
