package net.mangolise.gamesdk;

import net.hollowcube.polar.PolarLoader;
import net.mangolise.gamesdk.instance.InstanceAnalysis;
import net.mangolise.gamesdk.log.Log;
import net.mangolise.gamesdk.log.MangoliseLogbackLayout;
import net.minestom.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Paths;

public class Test {
    public static void main(String[] args) throws IOException {
        MangoliseLogbackLayout.init();
        MinecraftServer.init();

        PolarLoader loader = new PolarLoader(Paths.get("world.polar"));

        Log.logger().info("Analyzing...");

        long startTime = System.currentTimeMillis();
        InstanceAnalysis.analyse(loader.world());
        long endTime = System.currentTimeMillis();

        Log.logger().info("Analysis took {}ms", endTime - startTime);
    }
}
