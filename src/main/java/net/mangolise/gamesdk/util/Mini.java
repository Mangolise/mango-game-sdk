package net.mangolise.gamesdk.util;

import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Mini {

    private static final MiniMessage miniMessage = MiniMessage.builder()
            .build();

    public static Component message(String message) {
        return miniMessage.deserialize(message);
    }

    public static Component message(String message, Pointered pointered) {
        return miniMessage.deserialize(message, pointered);
    }
}
