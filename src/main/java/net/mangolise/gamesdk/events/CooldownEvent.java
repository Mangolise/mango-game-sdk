package net.mangolise.gamesdk.events;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.Material;

public class CooldownEvent implements Event {
    private final String name;
    private final long timeMs;
    private final Material icon;
    private final Player player;

    public CooldownEvent(String name, long timeMs, Material icon, Player player) {
        this.name = name;
        this.timeMs = timeMs;
        this.icon = icon;
        this.player = player;
    }

    public String name() {
        return name;
    }

    public long timeMs() {
        return timeMs;
    }

    public Material icon() {
        return icon;
    }

    public Player player() {
        return player;
    }
}
