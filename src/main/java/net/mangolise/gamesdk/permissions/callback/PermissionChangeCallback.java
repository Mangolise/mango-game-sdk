package net.mangolise.gamesdk.permissions.callback;

import net.minestom.server.entity.Player;

public interface PermissionChangeCallback {
    void onPermissionChange(Player player, String node);
}
