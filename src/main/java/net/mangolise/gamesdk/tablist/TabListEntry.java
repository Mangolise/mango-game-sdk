package net.mangolise.gamesdk.tablist;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.GameMode;

import java.util.UUID;

public record TabListEntry(UUID uuid, Component text, int latency, GameMode gameMode) {

    public static TabListEntry text(Component text) {
        return new TabListEntry(UUID.randomUUID(), text, 0, GameMode.SURVIVAL);
    }
}
