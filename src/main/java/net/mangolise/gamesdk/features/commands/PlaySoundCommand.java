package net.mangolise.gamesdk.features.commands;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

public class PlaySoundCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.playsound";
    }

    public PlaySoundCommand() {
        super("playsound");

        String[] sounds = SoundEvent.values().stream().map(sound -> sound.key().value()).toArray(String[]::new);

        addCheckedSyntax(this::execute,
                ArgumentType.Entity("target").onlyPlayers(true),
                ArgumentType.Word("sound").from(sounds),
                ArgumentType.Enum("source", Sound.Source.class).setFormat(ArgumentEnum.Format.LOWER_CASED).setDefaultValue(Sound.Source.MASTER),
                ArgumentType.Float("volume").setDefaultValue(1f),
                ArgumentType.Float("pitch").setDefaultValue(1f)
        );
    }

    private void execute(CommandSender sender, CommandContext context) {
        List<Entity> targets = context.<EntityFinder>get("target").find(sender);
        Sound.Source source = context.get("source");
        float volume = context.get("volume");
        float pitch = context.get("pitch");

        String strSound = context.get("sound");
        strSound = strSound.toLowerCase();

        for (SoundEvent sound : SoundEvent.values()) {
            if (sound.key().value().equals(strSound)) {
                for (Entity target : targets) {
                    ((Player) target).playSound(Sound.sound(sound, source, volume, pitch));
                }

                sender.sendMessage("Playing sound");
                return;
            }
        }

        sender.sendMessage("sound not found...");
    }
}
