package net.mangolise.gamesdk.events;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Will not be called unless the ExtraEventsFeature is used.
 */
public class PlayerAttemptDismountEvent implements PlayerEvent {
    private final Player player;
    private final Entity vehicle;

    public PlayerAttemptDismountEvent(@NotNull Player player, @NotNull Entity vehicle) {
        this.player = player;
        this.vehicle = vehicle;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public Entity getVehicle() {
        return vehicle;
    }
}
