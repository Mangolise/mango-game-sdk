package net.mangolise.gamesdk.tablist;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record TabListEntry(UUID uuid, Component text, int latency, GameMode gameMode, String username, @Nullable PlayerSkin skin) {

    public static TabListEntry text(Component text) {
        return new TabListEntry(UUID.randomUUID(), text, 0, GameMode.SURVIVAL, "asd", null);
    }

    public TabListEntry withUuid(UUID uuid) {
        return new TabListEntry(uuid, this.text, this.latency, this.gameMode, this.username, this.skin);
    }

    public TabListEntry withText(Component text) {
        return new TabListEntry(this.uuid, text, this.latency, this.gameMode, this.username, this.skin);
    }

    public TabListEntry withLatency(int latency) {
        return new TabListEntry(this.uuid, this.text, latency, this.gameMode, this.username, this.skin);
    }

    public TabListEntry withGameMode(GameMode gameMode) {
        return new TabListEntry(this.uuid, this.text, this.latency, gameMode, this.username, this.skin);
    }

    public TabListEntry withUsername(String username) {
        return new TabListEntry(this.uuid, this.text, this.latency, this.gameMode, username, this.skin);
    }

    public TabListEntry withSkin(@Nullable PlayerSkin skin) {
        return new TabListEntry(this.uuid, this.text, this.latency, this.gameMode, this.username, skin);
    }
}
