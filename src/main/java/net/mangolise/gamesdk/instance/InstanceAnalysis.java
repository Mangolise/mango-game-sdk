package net.mangolise.gamesdk.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.hollowcube.polar.PolarChunk;
import net.hollowcube.polar.PolarWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;

public class InstanceAnalysis {

    /**
     * Scans for blocks matching the given predicate.
     * <p>
     *     Note that this method will not scan for air blocks.
     * @param instance the instance to scan
     * @param blockPredicate the predicate to match
     * @return a map of points to blocks
     */
    public static Map<Point, Block> scanForBlocks(Instance instance, PolarWorld world, Predicate<Block> blockPredicate) {
        Map<Point, Block> blocks = new ConcurrentHashMap<>();

        analyse(instance, world).forEach((blockState, points) -> {
            Block block = Block.fromStateId(blockState);
            if (blockPredicate.test(block)) {
                for (Point point : points) {
                    blocks.put(point, block);
                }
            }
        });

        return Map.copyOf(blocks);
    }


    private static final Map<PolarWorld, Int2ObjectMap<Set<Point>>> COMPUTED_WORLDS = new ConcurrentHashMap<>();

    public static Int2ObjectMap<Set<Point>> analyse(Instance instance, PolarWorld world) {
        return COMPUTED_WORLDS.computeIfAbsent(world, ignored -> compute(instance, world));
    }

    private static Int2ObjectMap<Set<Point>> compute(Instance instance, PolarWorld world) {
        Int2ObjectMap<Set<Point>> blocks = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

        // for all chunks
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (PolarChunk polarChunk : world.chunks()) {
            futures.add(instance.loadChunk(polarChunk.x(), polarChunk.z()).thenAccept(chunk -> {
                DynamicChunk dynamicChunk = (DynamicChunk) chunk;

                boolean isChunkEmpty = true;
                for (Section section : dynamicChunk.getSections()) {
                    Palette blockPalette = section.blockPalette();
                    if (blockPalette.count() > 1) {
                        isChunkEmpty = false;
                        break;
                    }

                    if (blockPalette.count() == 1 && blockPalette.get(0, 0, 0) != 0){
                        isChunkEmpty = false;
                        break;
                    }
                }

                if (isChunkEmpty) {
                    return;
                }

                forEachNonAirBlockInChunk(chunk, (point, block) -> {
                    blocks.computeIfAbsent(block, ignored -> ConcurrentHashMap.newKeySet()).add(point);
                });
            }));
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        // make all sets unmodifiable
        Int2ObjectMap<Set<Point>> immutableBlocks = new Int2ObjectOpenHashMap<>();
        Int2ObjectMaps.fastForEach(blocks, (entry) -> immutableBlocks.put(entry.getIntKey(), Set.copyOf(entry.getValue())));
        return Int2ObjectMaps.unmodifiable(immutableBlocks);
    }

    private static void forEachNonAirBlockInChunk(Chunk chunk, ObjIntConsumer<Point> consumer) {
        // TODO: Optimize via accessing the direct palettes.
        // for each block in the chunk, check if it is air
        for (int blockX = chunk.getChunkX() * 16; blockX < chunk.getChunkX() * 16 + 16; blockX++) {
            for (int blockY = chunk.getMinSection() * 16; blockY < chunk.getMaxSection() * 16; blockY++) {
                for (int blockZ = chunk.getChunkZ() * 16; blockZ < chunk.getChunkZ() * 16 + 16; blockZ++) {
                    if (chunk.getBlock(blockX, blockY, blockZ).compare(Block.AIR)) {
                        continue;
                    }
                    consumer.accept(new Vec(blockX, blockY, blockZ), chunk.getBlock(blockX, blockY, blockZ).stateId());
                }
            }
        }
    }
}
