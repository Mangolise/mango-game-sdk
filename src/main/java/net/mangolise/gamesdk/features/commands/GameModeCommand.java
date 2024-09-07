package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Enum;

public class GameModeCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.gamemode";
    }

    public GameModeCommand() {
        super("gamemode");

        addPlayerSyntax(this::execute, Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED));
        addCheckedSyntax(this::executeTarget, Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED),
                ArgumentType.Entity("target").onlyPlayers(true));
    }

    private void execute(Player sender, CommandContext context) {
        GameMode gamemode = context.get("gamemode");
        sender.setGameMode(gamemode);
        sender.sendMessage(ChatUtil.toComponent("&aGamemode set to &6" + ChatUtil.capitaliseFirstLetter(gamemode.toString())));
    }

    private void executeTarget(CommandSender sender, CommandContext context) {
        GameMode gamemode = context.get("gamemode");
        List<Player> players = getPlayers(context, sender, "target");
        if (players == null) return;

        for (Player player : players) {
            player.setGameMode(gamemode);
        }

        sender.sendMessage(ChatUtil.toComponent("&aSet gamemode for &6%s &ato &6%s",
                context.getRaw("target"), ChatUtil.capitaliseFirstLetter(gamemode.toString())));
    }
}
