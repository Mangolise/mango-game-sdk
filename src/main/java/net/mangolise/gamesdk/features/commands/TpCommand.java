package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.util.ChatUtil;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;

import java.util.List;

public class TpCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.fly";
    }

    public TpCommand() {
        super("tp");

        // /tp 5 3 1
        addPlayerSyntax(this::executeToPos, ArgumentType.RelativeVec3("to"));

        // /tp Calcilore
        addPlayerSyntax(this::executeToEntity, ArgumentType.Entity("to").singleEntity(true));

        // /tp Calcilore ~ ~5 ~
        addCheckedSyntax(this::executeFromToPos, ArgumentType.Entity("entity"), ArgumentType.RelativeVec3("to"));

        // /tp Calcilore CoPokBl
        addCheckedSyntax(this::executeFromToEntity, ArgumentType.Entity("entity"), ArgumentType.Entity("to").singleEntity(true));
    }

    private void executeToPos(Player sender, CommandContext context) {
        RelativeVec to = context.get("to");
        execute(sender, sender, to);

        sendFeedbackVec(sender, to);
    }

    private void executeToEntity(Player sender, CommandContext context) {
        EntityFinder to = context.get("to");
        execute(sender, sender, to);
        sendFeedbackRaw(sender, context);
    }

    private void executeFromToPos(CommandSender sender, CommandContext context) {
        EntityFinder finder = context.get("entity");
        RelativeVec to = context.get("to");

        List<Entity> entities = finder.find(sender);
        if (entities.isEmpty()) {
            sender.sendMessage("No entity was found...");
            return;
        }

        for (Entity entity : entities) {
            execute(sender, entity, to);
        }

        sendFeedbackVec(sender, to);
    }

    private void executeFromToEntity(CommandSender sender, CommandContext context) {
        EntityFinder finder = context.get("entity");
        EntityFinder to = context.get("to");

        List<Entity> entities = finder.find(sender);
        if (entities.isEmpty()) {
            sender.sendMessage("No entity was found...");
            return;
        }

        for (Entity entity : entities) {
            execute(sender, entity, to);
        }

        sendFeedbackRaw(sender, context);
    }

    private void execute(CommandSender sender, Entity entity, RelativeVec position) {
        Pos entityPos = entity.getPosition();

        entity.teleport(position.fromSender(sender).asPosition().withView(entityPos.yaw(), entityPos.pitch()));
    }

    private void execute(CommandSender sender, Entity entity, EntityFinder position) {
        Entity found = position.findFirstEntity(sender);
        if (found == null) {
            sender.sendMessage("No entity was found...");
            return;
        }

        entity.teleport(found.getPosition());
    }

    private void sendFeedbackVec(CommandSender sender, RelativeVec pos) {
        sender.sendMessage(ChatUtil.toComponent("&aTeleported to " + GameSdkUtils.pointToString(pos.fromSender(sender))));
    }

    private void sendFeedbackRaw(CommandSender sender, CommandContext context) {
        sender.sendMessage(ChatUtil.toComponent("&aTeleported to " + context.getRaw("to")));
    }
}
