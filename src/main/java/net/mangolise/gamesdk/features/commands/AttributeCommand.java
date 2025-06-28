package net.mangolise.gamesdk.features.commands;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class AttributeCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.attribute";
    }

    public AttributeCommand() {
        super("attribute");

        String[] attributes = Attribute.values().stream().map(attribute -> attribute.key().value()).toArray(String[]::new);

        addCheckedSyntax(this::executeGet,
                ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true),
                ArgumentType.Word("attribute").from(attributes),
                ArgumentType.Literal("get")
        );

        addCheckedSyntax(this::executeBaseGet,
                ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true),
                ArgumentType.Word("attribute").from(attributes),
                ArgumentType.Literal("base"),
                ArgumentType.Literal("get")
        );

        addCheckedSyntax(this::executeBaseSet,
                ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true),
                ArgumentType.Word("attribute").from(attributes),
                ArgumentType.Literal("base"),
                ArgumentType.Literal("set"),
                ArgumentType.Double("value")
        );

        addCheckedSyntax(this::executeModifierGet,
                ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true),
                ArgumentType.Word("attribute").from(attributes),
                ArgumentType.Literal("modifier"),
                ArgumentType.Literal("get"),
                ArgumentType.String("id")
        );

        addCheckedSyntax(this::executeModifierList,
                ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true),
                ArgumentType.Word("attribute").from(attributes),
                ArgumentType.Literal("modifier"),
                ArgumentType.Literal("list")
        );

        addCheckedSyntax(this::executeModifierRemove,
                ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true),
                ArgumentType.Word("attribute").from(attributes),
                ArgumentType.Literal("modifier"),
                ArgumentType.Literal("remove"),
                ArgumentType.String("id")
        );

        addCheckedSyntax(this::executeModifierAdd,
                ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true),
                ArgumentType.Word("attribute").from(attributes),
                ArgumentType.Literal("modifier"),
                ArgumentType.Literal("add"),
                ArgumentType.String("id"),
                ArgumentType.Double("value"),
                ArgumentType.Enum("operation", AttributeOperation.class).setFormat(ArgumentEnum.Format.LOWER_CASED)
        );
    }

    private @Nullable Attribute findAttribute(CommandSender sender, CommandContext context) {
        String strAttribute = context.<String>get("attribute").toLowerCase();

        for (Attribute attribute : Attribute.values()) {
            if (attribute.key().value().equals(strAttribute)) {
                return attribute;
            }
        }

        sender.sendMessage("Attribute not found...");
        return null;
    }

    private @Nullable Player findPlayer(CommandSender sender, CommandContext context) {
        Player target = context.<EntityFinder>get("target").findFirstPlayer(sender);
        if (target == null) {
            sender.sendMessage("Player not found...");
        }

        return target;
    }

    private Key getId(CommandContext context) {
        return Key.key(context.<String>get("id"));
    }

    private String modifierToString(AttributeModifier modifier) {
        return String.format("%f (%s)", modifier.amount(), modifier.operation().name().toLowerCase().replace('_', ' '));
    }

    private void executeGet(CommandSender sender, CommandContext context) {
        Player target = findPlayer(sender, context);
        Attribute attribute = findAttribute(sender, context);
        if (attribute == null || target == null) return;

        sender.sendMessage(String.format("%s's %s is %f", target.getUsername(), attribute,
                target.getAttribute(attribute).getValue()));
    }

    private void executeBaseGet(CommandSender sender, CommandContext context) {
        Player target = findPlayer(sender, context);
        Attribute attribute = findAttribute(sender, context);
        if (attribute == null || target == null) return;

        sender.sendMessage(String.format("%s's base %s is %f", target.getUsername(), attribute,
                target.getAttribute(attribute).getBaseValue()));
    }

    private void executeBaseSet(CommandSender sender, CommandContext context) {
        Player target = findPlayer(sender, context);
        Attribute attribute = findAttribute(sender, context);
        double value = context.get("value");
        if (attribute == null || target == null) return;

        target.getAttribute(attribute).setBaseValue(value);
        sender.sendMessage(String.format("set %s's base %s to %f", target.getUsername(), attribute, value));
    }

    private void executeModifierGet(CommandSender sender, CommandContext context) {
        Player target = findPlayer(sender, context);
        Attribute attribute = findAttribute(sender, context);
        Key id = getId(context);
        if (attribute == null || target == null) return;

        AttributeModifier modifier = target.getAttribute(attribute).modifiers().stream()
                .filter(mod -> mod.id().equals(id)).findAny().orElse(null);

        if (modifier == null) {
            sender.sendMessage("modifier not found!");
            return;
        }

        sender.sendMessage(String.format("%s's modifier %s for %s is %s", target.getUsername(), id.value(), attribute,
                modifierToString(modifier)));
    }

    private void executeModifierList(CommandSender sender, CommandContext context) {
        Player target = findPlayer(sender, context);
        Attribute attribute = findAttribute(sender, context);
        if (attribute == null || target == null) return;

        Collection<AttributeModifier> modifiers = target.getAttribute(attribute).modifiers();
        if (modifiers.isEmpty()) {
            sender.sendMessage("No modifiers found.");
            return;
        }

        StringBuilder message = new StringBuilder(String.format("%s's modifiers for %s are:", target.getUsername(), attribute));
        for (AttributeModifier modifier : modifiers) {
            message.append(String.format("\n%s: %s", modifier.id(), modifierToString(modifier)));
        }

        sender.sendMessage(message.toString());
    }

    private void executeModifierRemove(CommandSender sender, CommandContext context) {
        Player target = findPlayer(sender, context);
        Attribute attribute = findAttribute(sender, context);
        Key id = getId(context);
        if (attribute == null || target == null) return;

        AttributeModifier modifier = target.getAttribute(attribute).removeModifier(id);
        if (modifier == null) {
            sender.sendMessage("modifier not found!");
            return;
        }

        sender.sendMessage(String.format("%s's modifier %s for %s was removed", target.getUsername(), id.value(), attribute));
    }

    private void executeModifierAdd(CommandSender sender, CommandContext context) {
        Player target = findPlayer(sender, context);
        Attribute attribute = findAttribute(sender, context);
        String id = context.get("id");
        double value = context.get("value");
        AttributeOperation operation = context.get("operation");
        if (attribute == null || target == null) return;

        AttributeModifier modifier = new AttributeModifier(id, value, operation);
        target.getAttribute(attribute).addModifier(modifier);

        sender.sendMessage(String.format("Added modifier %s to %s's %s", modifierToString(modifier), target.getUsername(), attribute));
    }
}
