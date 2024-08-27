package net.mangolise.gamesdk.features.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MangoliseCommand extends Command {
    public MangoliseCommand(@NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        setCondition((sender, s) -> !(sender instanceof Player player) || player.hasPermission(getPermission()));
    }

    protected @Nullable Player getAndCheckPlayer(CommandSender sender) {
        // setCondition only prevents autocomplete, not the execution itself
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You have to be a player to run this command!");
            return null;
        }

        if (!sender.hasPermission(getPermission())) {
            return null;
        }

        return player;
    }

    protected void addPlayerSyntax(PlayerCommandExecutor executor, Argument<?>... args) {
        addSyntax((sender, context) -> {
            Player player = getAndCheckPlayer(sender);
            if (player == null) return;

            executor.apply(player, context);
        }, args);
    }

    protected abstract String getPermission();

    @FunctionalInterface
    public interface PlayerCommandExecutor {

        /**
         * Executes the command callback once the syntax has been called (or the default executor).
         *
         * @param sender  the sender of the command
         * @param context the command context, used to retrieve the arguments and various other things
         */
        void apply(@NotNull Player sender, @NotNull CommandContext context);
    }
}
