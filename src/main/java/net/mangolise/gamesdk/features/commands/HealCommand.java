package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.utils.entity.EntityFinder;

public class HealCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.heal";
    }

    public HealCommand() {
        super("heal");
        addPlayerSyntax(this::executeNoArg);
        addCheckedSyntax(this::executeArg, ArgumentType.Entity("entity"));
    }

    private void executeNoArg(Player player, CommandContext context) {
        healEntity(player);
        player.sendMessage(ChatUtil.toComponent("&aHealed"));
    }

    private void executeArg(CommandSender sender, CommandContext context) {
        context.<EntityFinder>get("entity").find(sender).forEach(this::healEntity);
        sender.sendMessage(ChatUtil.toComponent("&aHealed"));
    }

    private void healEntity(Entity entity) {
        if (entity instanceof LivingEntity en) {
            en.setHealth((float) en.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }
    }
}
