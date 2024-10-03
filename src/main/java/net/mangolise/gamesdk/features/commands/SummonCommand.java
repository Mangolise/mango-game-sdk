package net.mangolise.gamesdk.features.commands;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;

public class SummonCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.summon";
    }

    public SummonCommand() {
        super("summon");
        addPlayerSyntax(this::executePos, ArgumentType.EntityType("entity"), ArgumentType.RelativeVec3("targetPos").setDefaultValue(CURRENT_POS_RELATIVEVEC));
        addPlayerSyntax(this::executeEntity, ArgumentType.EntityType("entity"), ArgumentType.Entity("target").singleEntity(true));
    }

    private void executePos(Player sender, CommandContext context) {
        EntityType entityType = context.get("entity");
        Point pos = context.<RelativeVec>get("targetPos").fromSender(sender);

        summonEntity(sender.getInstance(), pos, entityType);
        sender.sendMessage("Summoned new " + entityType.key().value());
    }

    private void executeEntity(Player sender, CommandContext context) {
        EntityType entityType = context.get("entity");
        Entity target = context.<EntityFinder>get("target").findFirstEntity(sender);
        if (target == null) {
            sender.sendMessage("Target entity not found");
            return;
        }

        summonEntity(sender.getInstance(), target.getPosition(), entityType);
        sender.sendMessage("Summoned new " + entityType.key().value());
    }

    private void summonEntity(Instance instance, Point pos, EntityType entityType) {
        Entity entity = new Entity(entityType);
        entity.setInstance(instance, pos);
    }
}
