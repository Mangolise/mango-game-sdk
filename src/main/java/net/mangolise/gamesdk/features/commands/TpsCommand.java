package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.util.ChatUtil;
import net.mangolise.gamesdk.util.PerformanceTracker;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

public class TpsCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.tps";
    }

    public TpsCommand() {
        super("tps", "performance");

        addPlayerSyntax(this::execute);
    }

    private void execute(Player sender, CommandContext context) {
        double tps = PerformanceTracker.getTps();
        double avTick = PerformanceTracker.getAverageTickDuration();
        long mbFree = Math.round(PerformanceTracker.getMbFree());
        long mbTotal = Math.round(PerformanceTracker.getMbTotal());
        long mbUsed = Math.round(PerformanceTracker.getMbUsed());
        sender.sendMessage(ChatUtil.toComponent("&aTPS: &7" + (tps == -1 ? "No data" : tps)));
        sender.sendMessage(ChatUtil.toComponent("&aAverage tick duration: &7" + (avTick == -1 ? "No data" : avTick + "ms")));
        sender.sendMessage(ChatUtil.toComponent("&aOnline players: &7" + MinecraftServer.getConnectionManager().getOnlinePlayerCount()));
        sender.sendMessage(ChatUtil.toComponent("&aMemory usage: &7" + mbUsed + "MB / " + mbTotal + "MB (" + mbFree + "MB free)"));
    }
}
