package net.mangolise.gamesdk.permissions.backends;

import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;

import java.util.HashMap;
import java.util.Map;

public class TagPermissionsBackend extends NodeStoragePermissionsBackend {
    public static Tag<Map<String, Boolean>> PERMISSIONS = Tag.<Map<String, Boolean>>Transient("gamesdk_permissions").defaultValue(HashMap::new);

    @Override
    public Map<String, Boolean> getNodes(Player player) {
        return player.getTag(PERMISSIONS);
    }

    @Override
    public void setNodes(Player player, Map<String, Boolean> nodes) {
        player.setTag(PERMISSIONS, new HashMap<>(nodes));
    }

    public void setPermission(Player player, String permission, boolean val) {
        player.getAndUpdateTag(PERMISSIONS, vals -> {
            vals.put(permission, val);
            return vals;
        });

        callbacks.runPermissionChangeCallback(player, permission);
    }

    public void removePermission(Player player, String permission) {
        player.getTag(PERMISSIONS).remove(permission);

        callbacks.runPermissionChangeCallback(player, permission);
    }
}
