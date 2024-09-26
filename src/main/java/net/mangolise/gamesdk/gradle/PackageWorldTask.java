package net.mangolise.gamesdk.gradle;

import com.google.gson.Gson;
import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Gradle task used to package anvil worlds to polar worlds
 */
public class PackageWorldTask extends DefaultTask {

    private static final Gson GSON = new Gson();

    @TaskAction
    public void packageWorld() {
        MinecraftServer.init();

        Path rootDir = getProject().getProjectDir().toPath();
        Path worldsDir = rootDir.resolve("worlds");
        Path worldsCache = rootDir.resolve("build").resolve(".worldscache.json");

        if (!worldsDir.toFile().exists()) {
            System.out.println("No worlds directory found, skipping world packaging");
            return;
        }

        try (Stream<Path> worldStream = Files.list(worldsDir)) {
            List<Path> worlds = worldStream.toList();

            // get the cache of world hashes from the last run
            Map<String, String> world2Hash;
            if (Files.exists(worldsCache)) {
                String worldHashFile = Files.readString(worldsCache);
                //noinspection unchecked
                world2Hash = new HashMap<String, String>(GSON.fromJson(worldHashFile, Map.class));
            } else {
                world2Hash = new HashMap<>();
            }


            if (worlds.isEmpty()) {
                System.out.println("No worlds found in " + worldsDir);
                return;
            }

            for (Path world : worlds) {
                if (!Files.isDirectory(world)) continue;
                String worldName = world.getFileName().toString();

                // add to resources
                Path worldPath = rootDir.resolve("src/main/resources/worlds/" + worldName + ".polar");

                // check the hash of the folder first
                String previousHash = world2Hash.get(worldName);
                String currentHash = hashDir(world);

                if (previousHash != null && previousHash.equals(currentHash)) {
                    System.out.println("skipping " + worldName + " as it hasn't changed");
                    continue;
                }

                System.out.print("packaging " + worldName + " to " + worldPath + "...");

                long startMs = System.currentTimeMillis();
                PolarWorld polarWorld = AnvilPolar.anvilToPolar(world);
                byte[] polarWorldBytes = PolarWriter.write(polarWorld);
                Files.createDirectories(worldPath.getParent());
                Files.deleteIfExists(worldPath);
                Files.write(worldPath, polarWorldBytes, StandardOpenOption.CREATE);
                long endMs = System.currentTimeMillis();

                System.out.println("done in " + (endMs - startMs) + "ms");
                world2Hash.put(worldName, currentHash);

                // update the cache
                String newCacheFileContents = GSON.toJson(world2Hash);
                if (Files.exists(worldsCache)) {
                    Files.delete(worldsCache);
                }
                Files.write(worldsCache, newCacheFileContents.getBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String hashDir(Path dir) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digestDirectory(dir, stream -> {
                try {
                    // update in increments to avoid loading the entire file into memory
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = stream.read(buffer)) != -1) {
                        md5.update(buffer, 0, read);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            byte[] digest = md5.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void digestDirectory(Path dir, Consumer<InputStream> acceptFileContents) {
        try (var files = Files.list(dir)) {
            files.forEach(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        acceptFileContents.accept(new ByteArrayInputStream(path.getFileName().toString().getBytes()));
                        digestDirectory(path, acceptFileContents);
                    } else {
                        InputStream stream = Files.newInputStream(path);
                        acceptFileContents.accept(stream);
                        stream.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
