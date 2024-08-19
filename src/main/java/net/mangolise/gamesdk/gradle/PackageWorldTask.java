package net.mangolise.gamesdk.gradle;

import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Gradle task used to package anvil worlds to polar worlds
 */
public class PackageWorldTask extends DefaultTask {

    @TaskAction
    public void packageWorld() {
        MinecraftServer.init();

        Path rootDir = getProject().getRootDir().toPath();
        Path worldsDir = rootDir.resolve("worlds");

        if (!worldsDir.toFile().exists()) {
            System.out.println("No worlds directory found, skipping world packaging");
            return;
        }

        try {
            List<Path> worlds = Files.list(worldsDir).toList();

            if (worlds.isEmpty()) {
                System.out.println("No worlds found in " + worldsDir);
                return;
            }

            for (Path world : worlds) {
                if (Files.isDirectory(world)) {
                    String worldName = world.getFileName().toString();

                    // add to resources
                    Path worldPath = rootDir.resolve("src/main/resources/worlds/" + worldName + ".polar");

                    System.out.print("packaging " + worldName + " to " + worldPath + "...");

                    long startMs = System.currentTimeMillis();
                    PolarWorld polarWorld = AnvilPolar.anvilToPolar(world);
                    byte[] polarWorldBytes = PolarWriter.write(polarWorld);
                    Files.createDirectories(worldPath.getParent());
                    Files.deleteIfExists(worldPath);
                    Files.write(worldPath, polarWorldBytes, StandardOpenOption.CREATE);
                    long endMs = System.currentTimeMillis();

                    System.out.println("done in " + (endMs - startMs) + "ms");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
