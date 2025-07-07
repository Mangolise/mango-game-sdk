package net.mangolise.gamesdk;

import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.key.Key;
import net.mangolise.gamesdk.instance.InstanceAnalysis;
import net.mangolise.gamesdk.log.Log;
import net.mangolise.gamesdk.log.MangoliseLogbackLayout;
import net.minestom.server.MinecraftServer;
import net.minestom.server.world.biome.Biome;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class InstanceAnalysisTest {

    @Test
    public void time() throws IOException {
        MangoliseLogbackLayout.init();
        MinecraftServer.init();
        MinecraftServer.getBiomeRegistry().register(Biome.PLAINS.key(), Biome.builder().build());

        PolarLoader loader = new PolarLoader(Paths.get("world.polar"));

        Log.logger().info("Analyzing...");

        long startTime = System.currentTimeMillis();
        InstanceAnalysis.analyse(loader.world());
        long endTime = System.currentTimeMillis();

        Log.logger().info("Analysis took {}ms", endTime - startTime);
    }
}
