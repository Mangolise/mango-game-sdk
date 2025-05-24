package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.permissions.Permissions;
import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MangoliseCommand extends Command {
    protected static final EntityFinder SELF_ENTITY_FINDER = new EntityFinder().setTargetSelector(EntityFinder.TargetSelector.SELF);
    protected static final RelativeVec CURRENT_POS_RELATIVEVEC = new RelativeVec(Vec.ZERO, RelativeVec.CoordinateType.RELATIVE, true, true, true);

    public MangoliseCommand(@NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        setCondition((sender, s) -> !(sender instanceof Player player) || hasPermission(player));
    }

    public boolean hasPermission(Player player) {
        return getPermission() == null || Permissions.hasPermission(player, getPermission());
    }

    protected List<Player> getPlayers(CommandContext context, CommandSender sender, String argument) {
        List<Entity> entities = context.<EntityFinder>get(argument).find(sender);
        if (entities.isEmpty()) {
            sender.sendMessage(ChatUtil.toComponent("&cCould not find any players..."));
            return null;
        }

        return entities.stream().map(entity -> (Player)entity).toList();
    }

    protected @Nullable Player getAndCheckPlayer(CommandSender sender) {
        // setCondition only prevents autocomplete, not the execution itself
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You have to be a player to run this command!");
            return null;
        }

        if (!hasPermission(player)) {
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

    protected void addCheckedSyntax(CommandExecutor executor, Argument<?>... args) {
        addConditionalSyntax(getCondition(), executor, args);
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
