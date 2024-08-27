package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.util.ChatUtil;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import static net.minestom.server.command.builder.arguments.ArgumentType.Enum;

public class GameModeCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.gamemode";
    }

    public GameModeCommand() {
        super("gamemode", "gm", "gmc", "gms", "gma", "gmsp");

        addPlayerSyntax(this::executeNoArgs);
        addPlayerSyntax(this::executeArgs, Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED));
    }

    private void executeNoArgs(Player sender, CommandContext context) {
        switch (context.getInput()) {
            case "gms" -> execute(sender, GameMode.SURVIVAL);
            case "gma" -> execute(sender, GameMode.ADVENTURE);
            case "gmc" -> execute(sender, GameMode.CREATIVE);
            case "gmsp" -> execute(sender, GameMode.SPECTATOR);
            default -> sender.sendMessage("Invalid gamemode!");
        }
    }

    private void executeArgs(Player sender, CommandContext context) {
        GameMode gamemode = context.get("gamemode");
        execute(sender, gamemode);
    }

    private void execute(Player sender, GameMode gamemode) {
        sender.setGameMode(gamemode);
        sender.sendMessage(ChatUtil.toComponent("&aGamemode set to &6" + GameSdkUtils.capitaliseFirstLetter(gamemode.toString())));
    }
}
