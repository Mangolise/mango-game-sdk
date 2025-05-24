package net.mangolise.gamesdk.permissions;

import net.mangolise.gamesdk.permissions.backends.TagPermissionsBackend;
import net.mangolise.gamesdk.permissions.callback.PermissionChangeCallback;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Permissions management class that provides a simple interface for setting,
 * removing, and checking permissions for players.
 * <p>
 * This class uses a backend to manage permissions, allowing for different
 * implementations of the permissions system.
 * <p>
 * The default backend is {@link TagPermissionsBackend}, which uses tags to manage permissions.
 * You can set a different backend using {@link #setBackend(PermissionsBackend)}.
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

    /**
     * Registers a callback for permission changes.
     * <p>
     * This callback will be invoked whenever a permission is changed with matching parameters.
     * @param player the player for whom the callback is registered, or null for all players
     * @param node the permission node to listen for changes, or null for all nodes (shouldn't have wildcards)
     * @param callback the callback to invoke when a permission change occurs
     * @return a Runnable that can be used to unregister the callback
     */
    public static Runnable registerCallback(@Nullable Player player, @Nullable String node, PermissionChangeCallback callback) {
        return backend.registerCallback(player, node, callback);
    }

    /**
     * Sets a permission for a player.
     * <p>
     * SETTING A PERMISSION TO FALSE IS NOT THE SAME AS REMOVING IT!
     * Setting a permission to false will explicitly deny it, whereas removing it will
     * allow the player to inherit permissions from wildcards.
     * <p>
     * For example, if you set "game.fly" to false, the player will not be able to fly, even
     * if they have "game.*" set to true. However, if you remove "game.fly", the player
     * will be able to inherit permissions from "game.*" if it is set to true.
     * @param player the player to set the permission for
     * @param permission the permission node to set, can have wildcards (e.g., "game.fly" or "game.*")
     * @param val the value to set the permission to, true for granting the permission, false for revoking it
     */
    public static void setPermission(Player player, String permission, boolean val) {
        backend.setPermission(player, permission, val);
    }

    /**
     * Removes a set permission from a player.
     * THIS IS DIFFERENT FROM SETTING IT TO FALSE!
     * <p>
     * Setting a permission to false will EXPLICITLY deny it, whereas removing it will
     * allow the player to inherit permissions from wildcards. See {@link #setPermission(Player, String, boolean)} for more details.
     * @param player the player to remove the permission from
     * @param permission the permission node to remove, must include any wildcards (e.g., "game.fly" and "game.*" are different)
     */
    public static void removePermission(Player player, String permission) {
        backend.removePermission(player, permission);
    }

    /**
     * Checks if a player has a specific permission.
     * @param player the player to check the permission for
     * @param node the permission node to check, should not contain wildcards
     * @return true if the player has the permission, false otherwise
     * @apiNote Although the node can technically contain wildcards because it will match the "*" as a valid match
     * for a wildcard, it is recommended to use exact nodes for clarity and to avoid unexpected behavior.
     */
    public static boolean hasPermission(Player player, String node) {
        return backend.hasPermission(player, node);
    }
}
