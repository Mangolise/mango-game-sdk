package net.mangolise.gamesdk.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.UUID;

public class Util {

    /**
     * Creates a fake uuid for an offline player
     */
    public static UUID createFakeUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer: " + name).getBytes());
    }

    /**
     * Get the configured port from the environment variables.
     * @return the configured port.
     */
    public static int getConfiguredPort() {
        return Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "25565"));
    }

    /**
     * Get the highest block at the given x and z coordinates.
     * @param instance the instance to check.
     * @param x the x coordinate.
     * @param z the z coordinate.
     * @return the y coordinate of the highest block.
     */
    public static int getHighestBlock(Instance instance, int x, int z) {
        int y = 255;
        while (instance.getBlock(x, y, z).isAir()) {
            y--;
        }
        return y;
    }

    /**
     * Get the spawn position for the given instance. The spawn position is the highest block at 0, 0.
     * Plus a little bit of give for the player to spawn on top of the block and not fall through the world.
     * @param instance the instance to get the spawn position for.
     * @return the spawn position.
     */
    public static Pos getSpawnPosition(Instance instance) {
        return new Pos(0, getHighestBlock(instance, 0, 0) + 2, 0);
    }
}
