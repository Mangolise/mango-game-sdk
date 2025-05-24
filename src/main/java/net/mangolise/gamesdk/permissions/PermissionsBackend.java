package net.mangolise.gamesdk.permissions;

import net.mangolise.gamesdk.permissions.callback.PermissionChangeCallback;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for a permissions backend that allows setting, removing, and checking
 * permissions for players.
 */
public interface PermissionsBackend {
    void setPermission(Player player, String permission, boolean val);
    void removePermission(Player player, String permission);
    boolean hasPermission(Player player, String node);
    Runnable registerCallback(@Nullable Player player, @Nullable String node, PermissionChangeCallback callback);
}
