package net.mangolise.gamesdk.util;

import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Deprecated(forRemoval = true)
public class Util {

    /**
     * Creates a fake uuid for an offline player
     * @deprecated use {@link GameSdkUtils} instead
     */
    @Deprecated(forRemoval = true)
    public static UUID createFakeUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer: " + name).getBytes());
    }

    /**
     * Get the configured port from the environment variables.
     * @return the configured port.
     * @deprecated use {@link GameSdkUtils} instead
     */
    @Deprecated(forRemoval = true)
    public static int getConfiguredPort() {
        return Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "25565"));
    }

    @Deprecated(forRemoval = true)
    /** @deprecated use {@link GameSdkUtils} instead */
    public static boolean useBungeeCord() {
        return Boolean.parseBoolean(System.getenv().getOrDefault("USE_BUNGEECORD", "false"));
    }

    /**
     * Get the highest block at the given x and z coordinates.
     * @param instance the instance to check.
     * @param x the x coordinate.
     * @param z the z coordinate.
     * @return the y coordinate of the highest block.
     * @deprecated use {@link GameSdkUtils} instead
     */
    @Deprecated(forRemoval = true)
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
     * @deprecated use {@link GameSdkUtils} instead
     */
    @Deprecated(forRemoval = true)
    public static Pos getSpawnPosition(Instance instance) {
        return new Pos(0.5, getHighestBlock(instance, 0, 0) + 2, 0.5);
    }

    /** @deprecated use {@link GameSdkUtils} instead */
    @Deprecated(forRemoval = true)
    public static PolarLoader getPolarLoaderFromResource(String path) {
        try {
            InputStream file = ClassLoader.getSystemResourceAsStream(path);
            if (file == null) {
                throw new RuntimeException("Could not find world file");
            }

            return new PolarLoader(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** @deprecated use {@link GameSdkUtils} instead */
    @Deprecated(forRemoval = true)
    public static void showTitle(Player player, int fadeIn, int stay, int fadeOut, Component title, Component subtitle) {
        Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        player.showTitle(Title.title(title, subtitle, times));
    }

    /** @deprecated use {@link GameSdkUtils} instead */
    @Deprecated(forRemoval = true)
    public static void broadcastMessage(Component message) {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
}
