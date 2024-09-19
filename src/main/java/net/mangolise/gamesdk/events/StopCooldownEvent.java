package net.mangolise.gamesdk.events;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

public class StopCooldownEvent implements Event {
    private final String name;
    private final Player player;

    public StopCooldownEvent(Player player, String name) {
        this.name = name;
        this.player = player;
    }

    public String name() {
        return name;
    }

    public Player player() {
        return player;
    }
}
