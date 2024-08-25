package net.mangolise.gamesdk.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class Timer {

    public static CompletableFuture<Void> countDown(long seconds, IntConsumer countDown) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        AtomicInteger counter = new AtomicInteger((int) seconds);
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            int count = counter.get();

            if (count == 0) {
                future.complete(null);
                return TaskSchedule.stop();
            }

            countDown.accept(count);
            counter.decrementAndGet();

            return TaskSchedule.seconds(1);
        }, TaskSchedule.immediate());

        return future;
    }

    public static CompletableFuture<Void> countDownForPlayer(int fromCount, Audience player) {
        return countDown(fromCount, count -> {
            Component component = Component.text(count).color(NamedTextColor.RED);
            Title.Times times = Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(500), Duration.ofMillis(100));
            player.showTitle(Title.title(Component.text(""), component, times));
        }).thenAccept(ignored -> player.clearTitle());
    }
}
