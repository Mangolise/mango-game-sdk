package net.mangolise.gamesdk.features.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

public class KillCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.kill";
    }

    public KillCommand() {
        super("kill");
        addCheckedSyntax(this::execute, ArgumentType.Entity("targets").setDefaultValue(SELF_ENTITY_FINDER));
    }

    private void execute(CommandSender sender, CommandContext context) {
        List<Entity> targets = context.<EntityFinder>get("targets").find(sender);

        targets.forEach(entity -> {
            if (entity instanceof LivingEntity target) {
                target.kill();
            }
        });

        sender.sendMessage("Did the killing");
    }
}
