package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.List;

public class FlyCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.fly";
    }

    public FlyCommand() {
        super("fly");

        addPlayerSyntax(this::execute);
        addCheckedSyntax(this::executeTarget, ArgumentType.Entity("target").onlyPlayers(true));
    }

    private void execute(Player sender, CommandContext context) {
        if (sender.isAllowFlying()) {
            sender.setAllowFlying(false);
            sender.setFlying(false);
            sender.sendMessage(ChatUtil.toComponent("&aYou can no longer fly"));
        } else {
            sender.setAllowFlying(true);
            sender.sendMessage(ChatUtil.toComponent("&aYou can now fly!"));
        }
    }

    private void executeTarget(CommandSender sender, CommandContext context) {
        List<Player> targets = getPlayers(context, sender, "target");
        if (targets == null) return;

        for (Player target : targets) {
            if (target.isAllowFlying()) {
                target.setAllowFlying(false);
                target.setFlying(false);
            } else {
                target.setAllowFlying(true);
            }
        }

        sender.sendMessage(ChatUtil.toComponent("&aToggled flying for " + context.getRaw("target")));
    }
}
