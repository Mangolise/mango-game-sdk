package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

// TODO: Account for armour in damage amounts
public class LavaHurtFeature implements Game.Feature<Game> {
    private static final Tag<Long> PLAYER_DAMAGE_START = Tag.Long("gamesdk_lavadamage_starttime");

    @Override
    public void setup(Context<Game> context) {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerTickEvent.class, this::playerTick);
    }

    private void playerTick(@NotNull PlayerTickEvent event) {
        Player player = event.getPlayer();
        Set<Integer> blocks = GameSdkUtils.getBlockIdsPlayerIsStandingOnAndAbove(player, 2, true);
        if (!blocks.contains(Block.LAVA.id())) {
            player.removeTag(PLAYER_DAMAGE_START);
            return;
        }

        if (!player.hasTag(PLAYER_DAMAGE_START)) {
            player.setTag(PLAYER_DAMAGE_START, player.getInstance().getWorldAge());
        }

        long start = player.getTag(PLAYER_DAMAGE_START);
        if ((player.getInstance().getWorldAge() - start) % 10 != 0) return;

        // Tick every half second
        player.damage(new Damage(DamageType.LAVA, null, null, null, 4f));
        player.setFireTicks(300);
    }
}
