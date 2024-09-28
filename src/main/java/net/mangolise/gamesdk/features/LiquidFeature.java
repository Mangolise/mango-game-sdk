package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Supplier;

public class LiquidFeature implements Game.Feature<Game> {
    private static final Set<Point> surrounding = Set.of(
            new BlockVec(1, 0, 0), new BlockVec(-1, 0, 0),
            new BlockVec(0, 0, 1), new BlockVec(0, 0, -1)
    );

    @Override
    public void setup(Context<Game> context) {
        createHandler(Block.WATER, 5, true);
        createHandler(Block.LAVA, 30, false);
    }

    private static void createHandler(Block liquid, int flowSpeed, boolean canInfinite) {
        MinecraftServer.getBlockManager().registerHandler(liquid.namespace(),
                () -> new LiquidSourceHandler(liquid, flowSpeed, canInfinite));
    }

    public static int getDefaultFlowSpeed(Block block) {
        return block.compare(Block.LAVA) ? 30 : 5;
    }

    public static boolean getDefaultInfiniteSource(Block block) {
        return !block.compare(Block.LAVA);
    }

    public static BlockHandler getSourceHandler(Block block, int flowSpeed, boolean canInfinite) {
        return new LiquidSourceHandler(block, flowSpeed, canInfinite);
    }

    public static BlockHandler getFlowingHandler(Block block, int flowSpeed, boolean falling, boolean canInfinite) {
        return new LiquidFlowHandler(block, flowSpeed, falling, canInfinite);
    }

    private static class LiquidHandler implements BlockHandler {
        protected final Block block;
        protected final int flowSpeed;
        protected final boolean canInfinite;
        private int index = 0;

        public LiquidHandler(Block block, int flowSpeed, boolean canInfinite) {
            this.block = block;
            this.flowSpeed = flowSpeed;
            this.canInfinite = canInfinite;
        }

        @Override
        public @NotNull NamespaceID getNamespaceId() {
            return block.namespace();
        }

        @Override
        public boolean isTickable() {
            return true;
        }

        public boolean cantTick() {
            index++;
            return index % flowSpeed != 0;
        }
    }

    private static class LiquidSourceHandler extends LiquidHandler {
        public LiquidSourceHandler(Block block, int flowSpeed, boolean canInfinite) {
            super(block, flowSpeed, canInfinite);
        }

        @Override
        public void tick(@NotNull Tick tick) {
            if (cantTick()) return;

            Instance instance = tick.getInstance();
            Point blockPos = tick.getBlockPosition();

            for (Point pos : surrounding) {
                pos = blockPos.add(pos);
                tryPlaceBlock(instance, pos, 7, () -> block
                        .withHandler(new LiquidFlowHandler(block, flowSpeed, false, canInfinite)));
            }

            tryPlaceBlock(instance, blockPos.sub(0, 1, 0), 7, () -> block
                    .withHandler(new LiquidFlowHandler(block, flowSpeed, true, canInfinite)));
        }
    }

    private static class LiquidFlowHandler extends LiquidHandler {
        private final boolean falling;

        public LiquidFlowHandler(Block block, int flowSpeed, boolean falling, boolean canInfinite) {
            super(block, flowSpeed, canInfinite);
            this.falling = falling;
        }

        @Override
        public void tick(@NotNull Tick tick) {
            if (cantTick()) return;

            int level = getLevel(tick.getBlock());

            Instance instance = tick.getInstance();
            Point blockPos = tick.getBlockPosition();

            // check if there is a water block for this water to come from
            if (falling ? !hasUpSupportingBlock(instance, blockPos) : horizSupportingBlockCount(instance, blockPos, level) == 0) {
                if (falling || level == 1) {
                    instance.setBlock(blockPos, Block.AIR);
                } else {
                    placeBlock(instance, blockPos, level - 1, block.withHandler(this));
                }
                return;
            }

            // if there are 2 nearby source blocks, this become s a source block
            if (!falling && canInfinite && horizSupportingBlockCount(instance, blockPos, 7) >= 2) {
                placeBlock(instance, blockPos, 8, block.withHandler(new LiquidSourceHandler(block, flowSpeed, canInfinite)));
            }

            // try placing below
            Point belowPos = tick.getBlockPosition().sub(0, 1, 0);
            Block belowBlock = instance.getBlock(belowPos);

            if (belowBlock.compare(block)) {
                return;
            }

            if (tryPlaceBlock(instance, belowPos, belowBlock, 7, () -> block
                    .withHandler(new LiquidFlowHandler(block, flowSpeed, true, canInfinite)))) {
                return;
            }

            // if not, then go horizontally
            if (level > 1) {
                level--;
                for (Point pos : surrounding) {
                    pos = blockPos.add(pos);
                    tryPlaceBlock(instance, pos, level, () -> block.withHandler(new LiquidFlowHandler(block, flowSpeed, false, canInfinite)));
                }
            }
        }

        private int horizSupportingBlockCount(Instance instance, Point blockPos, int level) {
            int count = 0;

            for (Point pos : surrounding) {
                pos = blockPos.add(pos);
                Block neighbor = instance.getBlock(pos);
                if (!neighbor.compare(block)) {
                    continue;
                }

                int nLevel = getLevel(neighbor);
                if (nLevel == level + 1) {
                    count++;
                }
            }

            return count;
        }

        private boolean hasUpSupportingBlock(Instance instance, Point blockPos) {
            blockPos = blockPos.add(0, 1, 0);
            Block neighbor = instance.getBlock(blockPos);
            return neighbor.compare(block);
        }
    }

    private static boolean tryPlaceBlock(Instance instance, Point pos, int level, Supplier<Block> block) {
        return tryPlaceBlock(instance, pos, instance.getBlock(pos), level, block);
    }

    // Level has to be inverted for the client for some reason
    private static boolean tryPlaceBlock(Instance instance, Point pos, Block currentBlock, int level, Supplier<Block> block) {
        if (currentBlock.isAir() || (currentBlock.compare(Block.WATER) && getLevel(currentBlock) < level)) {
            placeBlock(instance, pos, level, block.get());
            return true;
        }

        return false;
    }

    private static void placeBlock(Instance instance, Point pos, int level, Block block) {
        instance.setBlock(pos, block.withProperty("level", String.valueOf(8-level)));
    }

    // Level has to be inverted for the client for some reason
    public static int getLevel(Block block) {
        return 8 - Integer.parseInt(block.getProperty("level"));
    }
}
