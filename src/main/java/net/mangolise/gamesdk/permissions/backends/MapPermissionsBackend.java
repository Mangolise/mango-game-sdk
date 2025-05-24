package net.mangolise.gamesdk.permissions.backends;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApiStatus.Internal
public class MapPermissionsBackend extends NodeStoragePermissionsBackend {
    public static Map<UUID, Map<String, Boolean>> permissions = new HashMap<>();

    @Override
    public Map<String, Boolean> getNodes(Player player) {
        return permissions.computeIfAbsent(player.getUuid(), k -> new HashMap<>());
    }

    @Override
    public void setNodes(Player player, Map<String, Boolean> nodes) {
        permissions.put(player.getUuid(), new HashMap<>(nodes));
    }

    public void setPermission(Player player, String permission, boolean val) {
        getNodes(player).put(permission, val);

        callbacks.runPermissionChangeCallback(player, permission);
    }

    public void removePermission(Player player, String permission) {
        getNodes(player).remove(permission);

        callbacks.runPermissionChangeCallback(player, permission);
    }
}
