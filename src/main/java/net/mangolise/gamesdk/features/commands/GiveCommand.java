package net.mangolise.gamesdk.features.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

public class GiveCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.give";
    }

    public GiveCommand() {
        super("give", "i");
        addCheckedSyntax(this::execute, ArgumentType.ItemStack("item"),
                ArgumentType.Integer("count").setDefaultValue(1),
                ArgumentType.Entity("targets").onlyPlayers(true).setDefaultValue(SELF_ENTITY_FINDER));
    }

    private void execute(CommandSender sender, CommandContext context) {
        int count = context.get("count");
        ItemStack item = context.<ItemStack>get("item").withAmount(count);
        List<Entity> targets = context.<EntityFinder>get("targets").find(sender);

        for (Entity entity : targets) {
            if (entity instanceof Player target) {
                target.getInventory().addItemStack(item, TransactionOption.ALL);
            }
        }

        sender.sendMessage("Gave " + count + " " + item.material().key().value());
    }
}
