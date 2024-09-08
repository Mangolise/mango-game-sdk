package net.mangolise.gamesdk.util;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.instance.InstanceTickEvent;

import java.util.Arrays;

public class PerformanceTracker {
    private static final int TICKS_SAMPLE_SIZE = 20 * 10;  // about 10 seconds

    private static final int[] durations = new int[TICKS_SAMPLE_SIZE];
    private static int durationsIndex = 0;

    private static final long[] tickTimes = new long[TICKS_SAMPLE_SIZE];
    private static int tickTimesIndex = 0;

    public static void start() {
        Arrays.fill(durations, 0);
        Arrays.fill(tickTimes, -1);

        MinecraftServer.getGlobalEventHandler().addListener(InstanceTickEvent.class, event -> {
            durations[durationsIndex] = event.getDuration();
            durationsIndex = (durationsIndex + 1) % durations.length;

            tickTimes[tickTimesIndex] = System.nanoTime();
            tickTimesIndex = (tickTimesIndex + 1) % tickTimes.length;
        });
    }

    /**
     * Get the current TPS.
     * @return the current TPS, or -1 if the TPS cannot be calculated.
     */
    public static double getTps() {
        long now = System.nanoTime();
        long oldest = tickTimes[tickTimesIndex];

        if (oldest == -1) {
            return -1;
        }

        long elapsed = now - oldest;
        return ((double) tickTimes.length) / ((double) elapsed / 1_000_000_000.0D);
    }

    /**
     * Get the average tick duration.
     * @return the average tick duration in nanoseconds. Returns -1 if no data is available.
     */
    public static double getAverageTickDuration() {
        if (durations[durationsIndex] == -1) {
            return -1;
        }

        return Arrays.stream(durations).average().orElse(-1);
    }

    public static double getMbUsed() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0;
    }

    public static double getMbTotal() {
        return Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0;
    }

    public static double getMbFree() {
        return Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0;
    }
}
