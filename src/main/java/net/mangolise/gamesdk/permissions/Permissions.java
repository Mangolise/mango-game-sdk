package net.mangolise.gamesdk.permissions;

import net.mangolise.gamesdk.permissions.backends.TagPermissionsBackend;
import net.minestom.server.entity.Player;

public class Permissions {
    private static IPermissionsBackend backend = new TagPermissionsBackend();

    public static void setBackend(IPermissionsBackend backend) {
        Permissions.backend = backend;
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
