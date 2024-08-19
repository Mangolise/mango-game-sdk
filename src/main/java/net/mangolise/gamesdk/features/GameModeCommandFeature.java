package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import static net.minestom.server.command.builder.arguments.ArgumentType.Enum;

public class GameModeCommandFeature implements Game.Feature<Game> {
    @Override
    public void setup(Context<Game> context) {
        MinecraftServer.getCommandManager().register(new GameModeCommand());
    }

    private static class GameModeCommand extends Command {
        public GameModeCommand() {
            super("gamemode", "gm", "gmc", "gms", "gma", "gmsp");

            // TODO: Add permissions
            setCondition((sender, s) -> sender instanceof Player);

            addSyntax(this::executeNoArgs);
            addSyntax(this::executeArgs, Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED));
        }

        private void executeNoArgs(CommandSender sender, CommandContext context) {
            switch (context.getInput()) {
                case "gms" -> execute(sender, GameMode.SURVIVAL);
                case "gma" -> execute(sender, GameMode.ADVENTURE);
                case "gmc" -> execute(sender, GameMode.CREATIVE);
                case "gmsp" -> execute(sender, GameMode.SPECTATOR);
                default -> sender.sendMessage("Invalid gamemode!");
            }
        }

        private void executeArgs(CommandSender sender, CommandContext context) {
            GameMode gamemode = context.get("gamemode");
            execute(sender, gamemode);
        }

        private void execute(CommandSender sender, GameMode gamemode) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You have to be a player to run this command!");
                return;
            }

            player.setGameMode(gamemode);
        }
    }
}
