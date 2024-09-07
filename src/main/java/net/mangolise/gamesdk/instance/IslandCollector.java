package net.mangolise.gamesdk.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Segments a set of blocks into groups that are directly touching each other. (not diagonally touching)
 */
public class IslandCollector {

    public record Island(Point center, Set<Point> points) {}

    /** Collects the slabs into islands. */
    public static Set<Island> collectIntoIslands(Map<Point, Block> blocks) {
        List<Map<Point, Block>> unjoinedIslands = new ArrayList<>();

        for (Map.Entry<Point, Block> entry : blocks.entrySet()) {
            Point point = entry.getKey();
            Block block = entry.getValue();

            for (Map<Point, Block> island : unjoinedIslands) {
                for (Point pos : Set.copyOf(island.keySet())) {
                    if (point.distance(pos) <= 1) {
                        island.put(point, block);
                        break;
                    }
                }
            }

            Map<Point, Block> newIsland = new HashMap<>();
            newIsland.put(point, block);
            unjoinedIslands.add(newIsland);
        }

        // join all islands
        outer:
        while (true) {
            for (int i = 0; i < unjoinedIslands.size(); i++) {
                for (int j = i + 1; j < unjoinedIslands.size(); j++) {
                    Map<Point, Block> joined = tryJoin(unjoinedIslands.get(i), unjoinedIslands.get(j));
                    if (joined != null) {
                        unjoinedIslands.set(i, joined);
                        unjoinedIslands.remove(j);
                        continue outer;
                    }
                }
            }
            break;
        }

        // all islands are now joined.
        Set<Map<Point, Block>> joinedIslands = Set.copyOf(unjoinedIslands);

        return joinedIslands.stream()
                .map(island -> {
                    // find center
                    double x = 0, y = 0, z = 0;
                    for (Point point : island.keySet()) {
                        x += point.x();
                        y += point.y();
                        z += point.z();
                    }
                    Point center = new Vec(x / island.size(), y / island.size(), z / island.size());
                    center = center.add(0.5); // center in middle of block

                    return new Island(center, island.keySet());
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Tries to join two islands if they are within 1 block of each other. */
    private static @Nullable Map<Point, Block> tryJoin(Map<Point, Block> islandA, Map<Point, Block> islandB) {
        for (Point pos : islandA.keySet()) {
            for (Point otherPos : islandB.keySet()) {
                if (pos.distance(otherPos) <= 1) {
                    Map<Point, Block> newIsland = new HashMap<>();
                    newIsland.putAll(islandA);
                    newIsland.putAll(islandB);
                    return Collections.unmodifiableMap(newIsland);
                }
            }
        }
        return null;
    }
}
