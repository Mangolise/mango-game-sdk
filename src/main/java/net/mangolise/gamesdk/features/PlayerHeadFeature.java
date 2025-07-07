package net.mangolise.gamesdk.features;

import net.kyori.adventure.key.Key;
import net.mangolise.gamesdk.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class PlayerHeadFeature implements Game.Feature<Game> {
    @Override
    public void setup(Context<Game> context) {
        MinecraftServer.getBlockManager().registerHandler("minecraft:skull", () -> PlayerHeadBlock.INSTANCE);
    }

    private enum PlayerHeadBlock implements BlockHandler {
        INSTANCE;

        @Override
        public @NotNull Key getKey() {
            return Key.key("minecraft:skull");
        }

        @Override
        public @NotNull Collection<Tag<?>> getBlockEntityTags() {
            return Set.of(
                    Tag.String("custom_name"),
                    Tag.String("note_block_sound"),
                    Tag.NBT("profile")
            );
        }
    }
}
