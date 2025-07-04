package net.mangolise.gamesdk.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.hollowcube.polar.PolarChunk;
import net.hollowcube.polar.PolarSection;
import net.hollowcube.polar.PolarWorld;
import net.mangolise.gamesdk.log.Log;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class InstanceAnalysis {

    /**
     * Scans for blocks matching the given predicate.
     * <p>
     *     Note that this method will not scan for air blocks.
     * @param blockPredicate the predicate to match
     * @return a map of points to blocks
     */
    public static Map<Point, Block> scanForBlocks(PolarWorld world, Predicate<Block> blockPredicate) {
        Map<Point, Block> blocks = new ConcurrentHashMap<>();

        analyse(world).forEach((blockState, points) -> {
            Block block = Block.fromStateId(blockState);
            if (blockPredicate.test(block)) {
                for (Point point : points) {
                    blocks.put(point, block);
                }
            }
        });

        return Map.copyOf(blocks);
    }

    private static final Map<PolarWorld, Int2ObjectMap<List<Point>>> COMPUTED_WORLDS = new ConcurrentHashMap<>();

    public static Int2ObjectMap<List<Point>> analyse(PolarWorld world) {
        return COMPUTED_WORLDS.computeIfAbsent(world, ignored -> compute(world));
    }

    private static Int2ObjectMap<List<Point>> compute(PolarWorld world) {
        Int2ObjectMap<List<Point>> blocks = new Int2ObjectOpenHashMap<>();

        // for all chunks
        List<CompletableFuture<?>> futures = new ArrayList<>();

        int minSection = world.minSection();

        for (PolarChunk polarChunk : world.chunks()) {
            CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
                Int2ObjectMap<List<Point>> chunkBlocks = computeChunk(minSection, polarChunk);
                synchronized (blocks) {
                    Int2ObjectMaps.fastForEach(chunkBlocks, (entry) -> {
                        blocks.computeIfAbsent(entry.getIntKey(), ignored -> new ArrayList<>()).addAll(entry.getValue());
                    });
                }
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        Log.logger().info("Found {} unique block states in this world.", globalNamespace2stateId.size());

        return Int2ObjectMaps.unmodifiable(blocks);
    }

    private static final Map<String, Integer> globalNamespace2stateId = new ConcurrentHashMap<>();
    private static final ArgumentBlockState blockStateParser = new ArgumentBlockState("");
    private static int safeGetStateId(String namespace) {
        return globalNamespace2stateId.computeIfAbsent(namespace, ignored -> {
            return blockStateParser.parse(MinecraftServer.getCommandManager().getConsoleSender(), namespace).stateId();
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Int2ObjectMap<List<Point>> computeChunk(int minSection, PolarChunk polarChunk) {
        Int2ObjectMap<List<Point>> blocks = new Int2ObjectOpenHashMap<>();
        Object2IntMap<String> namespace2stateId = new Object2IntOpenHashMap<>();

        // for all chunks
        int chunkX = polarChunk.x();
        int chunkZ = polarChunk.z();

        PolarSection[] sections = polarChunk.sections();
        for (int sectionIndex = 0, sectionsLength = sections.length; sectionIndex < sectionsLength; sectionIndex++) {
            PolarSection section = sections[sectionIndex];
            int sectionY = sectionIndex + minSection;
            String[] palette = section.blockPalette();
            int[] blockIndexes = section.blockData();

            if (palette.length == 1) {
                String stateString = palette[0];
                int stateId = namespace2stateId.computeIfAbsent(stateString, ignored -> safeGetStateId(stateString));
                if (stateId == Block.AIR.stateId()) {
                    continue;
                }

                // if the section is not air, then it is a full section
                // so simply add all the blocks in the section
                blockIndexes = new int[Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE];
                Arrays.fill(blockIndexes, 0);
            }

            for (int i = 0; i < blockIndexes.length; i++) {
                int paletteIndex = blockIndexes[i];

                // get the relative position of the block
                int relY = i / Chunk.CHUNK_SECTION_SIZE / Chunk.CHUNK_SECTION_SIZE;
                int relZ = (i / Chunk.CHUNK_SECTION_SIZE) % Chunk.CHUNK_SECTION_SIZE;
                int relX = i % Chunk.CHUNK_SECTION_SIZE;

                // compute the absolute position of the block
                int absX = chunkX * Chunk.CHUNK_SECTION_SIZE + relX;
                int absY = sectionY * Chunk.CHUNK_SECTION_SIZE + relY;
                int absZ = chunkZ * Chunk.CHUNK_SECTION_SIZE + relZ;

                // get the stateid if we haven't already
                String namespace = palette[paletteIndex];
                int stateId = namespace2stateId.computeIfAbsent(namespace, ignored -> safeGetStateId(namespace));
                if (stateId == Block.AIR.stateId()) continue;

                blocks.computeIfAbsent(stateId, ignored -> new ArrayList<>()).add(new Vec(absX, absY, absZ));
            }
        }

        // make all sets unmodifiable
        Int2ObjectMap<List<Point>> immutableBlocks = new Int2ObjectOpenHashMap<>();
        Int2ObjectMaps.fastForEach(blocks, (entry) -> immutableBlocks.put(entry.getIntKey(), Collections.unmodifiableList(entry.getValue())));
        return Int2ObjectMaps.unmodifiable(immutableBlocks);
    }
}
