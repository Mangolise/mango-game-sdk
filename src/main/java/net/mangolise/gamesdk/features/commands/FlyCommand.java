package net.mangolise.gamesdk.features.commands;

import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

public class FlyCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.fly";
    }

    public FlyCommand() {
        super("fly");

        addPlayerSyntax(this::execute);
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
}
