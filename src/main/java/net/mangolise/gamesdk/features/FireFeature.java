package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.BaseGame;
import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class FireFeature implements Game.Feature<BaseGame<?>> {
    private static final List<Integer> EXTINGUISH_BLOCKS = List.of(
            Block.WATER.id(),
            Block.POWDER_SNOW.id()
    );

    private static final Tag<Long> PLAYER_DAMAGE_START = Tag.Long("gamesdk_firedamage_starttime");
    private boolean lavaEnabled;

    @Override
    public void setup(Context<BaseGame<?>> context) {
        lavaEnabled = context.game().hasFeature(LavaHurtFeature.class);
        context.eventNode().addListener(PlayerTickEvent.class, this::playerTick);
    }

    private void playerTick(@NotNull PlayerTickEvent event) {
        Player player = event.getPlayer();

        Set<Integer> blocks = GameSdkUtils.getBlockIdsPlayerIsStandingOnAndAbove(player, 2, true);
        boolean inFire = blocks.contains(Block.FIRE.id());

        if (player.isOnFire() && blocks.stream().anyMatch(EXTINGUISH_BLOCKS::contains)) {
            player.setFireTicks(0);
        }

        if (!player.isOnFire() && !inFire) {
            player.removeTag(PLAYER_DAMAGE_START);
            return;
        }

        if (inFire) {
            player.setFireTicks(160);
        }

        if (!player.hasTag(PLAYER_DAMAGE_START)) {
            player.setTag(PLAYER_DAMAGE_START, player.getInstance().getWorldAge());
        }

        int tickPeriod = inFire ? 10 : 20;
        long start = player.getTag(PLAYER_DAMAGE_START);
        if ((player.getInstance().getWorldAge() - start) % tickPeriod != 0) return;

        if (blocks.contains(Block.LAVA.id()) && lavaEnabled) {
            return;  // Don't deal fire and lava damage
        }

        // Tick every half second
        player.damage(new Damage(DamageType.LAVA, null, null, null, 1f));
    }
}
