package net.mangolise.gamesdk.permissions;

import net.mangolise.gamesdk.permissions.backends.TagPermissionsBackend;
import net.mangolise.gamesdk.permissions.callback.PermissionChangeCallback;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Permissions management class that provides a simple interface for setting,
 * removing, and checking permissions for players.
 */
public class Permissions {
    private static PermissionsBackend backend = new TagPermissionsBackend();

    /**
     * Sets the backend for permissions management.
     * @param backend The permissions backend to use.
     */
    public static void setBackend(PermissionsBackend backend) {
        Permissions.backend = backend;
    }

    public static Runnable registerCallback(@Nullable Player player, @Nullable String node, PermissionChangeCallback callback) {
        return backend.registerCallback(player, node, callback);
    }

    public static void setPermission(Player player, String permission, boolean val) {
        backend.setPermission(player, permission, val);
    }

    public static void removePermission(Player player, String permission) {
        backend.removePermission(player, permission);
    }

    public static boolean hasPermission(Player player, String node) {
        return backend.hasPermission(player, node);
    }
}
