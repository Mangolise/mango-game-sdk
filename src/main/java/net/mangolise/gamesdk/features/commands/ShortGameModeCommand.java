package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import java.util.List;

public class ShortGameModeCommand extends MangoliseCommand {
    private final GameMode gamemode;

    @Override
    protected String getPermission() {
        return "mangolise.command.gamemode";
    }

    public ShortGameModeCommand(String commandName, GameMode gamemode) {
        super(commandName);
        this.gamemode = gamemode;

        addPlayerSyntax(this::execute);
        addCheckedSyntax(this::executeTarget, ArgumentType.Entity("target").onlyPlayers(true));
    }

    private void execute(Player player, CommandContext context) {
        player.setGameMode(gamemode);
        player.sendMessage(ChatUtil.toComponent("&aGamemode set to &6" + ChatUtil.capitaliseFirstLetter(gamemode.name())));
    }

    private void executeTarget(CommandSender sender, CommandContext context) {
        List<Player> targets = getPlayers(context, sender, "target");
        if (targets == null) return;

        for (Player player : targets) {
            player.setGameMode(gamemode);
        }

        sender.sendMessage(ChatUtil.toComponent("&aSet gamemode for &6%s &ato &6%s",
                context.getRaw("target"), ChatUtil.capitaliseFirstLetter(gamemode.name())));
    }
}
