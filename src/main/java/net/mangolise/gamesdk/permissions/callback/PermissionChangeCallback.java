package net.mangolise.gamesdk.permissions.callback;

import net.minestom.server.entity.Player;

/**
 * Callback interface for permission changes.
 * This interface is used to notify when a player's permission changes.
 */
public interface PermissionChangeCallback {
    void onPermissionChange(Player player, String node);
}
