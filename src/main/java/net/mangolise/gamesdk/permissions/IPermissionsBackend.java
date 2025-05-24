package net.mangolise.gamesdk.permissions;

import net.minestom.server.entity.Player;

public interface IPermissionsBackend {
    void setPermission(Player player, String permission, boolean val);
    void removePermission(Player player, String permission);
    boolean hasPermission(Player player, String node);
}
