package net.mangolise.gamesdk.permissions.backends;

import net.mangolise.gamesdk.permissions.PermissionsBackend;
import net.mangolise.gamesdk.permissions.callback.PermissionChangeCallback;
import net.mangolise.gamesdk.permissions.callback.PermissionChangeCallbackSystem;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class NodeStoragePermissionsBackend implements PermissionsBackend {
    protected PermissionChangeCallbackSystem callbacks = new PermissionChangeCallbackSystem();

    public abstract Map<String, Boolean> getNodes(Player player);  // can be immutable return
    public abstract void setNodes(Player player, Map<String, Boolean> nodes);

    @Override
    public Runnable registerCallback(@Nullable Player player, @Nullable String node, PermissionChangeCallback callback) {
        return callbacks.registerCallback(player, node, callback);
    }

    @Override
    public void setPermission(Player player, String permission, boolean val) {
        Map<String, Boolean> vals = new HashMap<>(getNodes(player));
        vals.put(permission, val);
        setNodes(player, vals);

        callbacks.runPermissionChangeCallback(player, permission);
    }

    @Override
    public void removePermission(Player player, String permission) {
        Map<String, Boolean> vals = new HashMap<>(getNodes(player));
        vals.remove(permission);
        setNodes(player, vals);

        callbacks.runPermissionChangeCallback(player, permission);
    }

    @Override
    public boolean hasPermission(Player player, String node) {
        boolean matchesPos = false;
        Pattern regex = Pattern.compile("^(" + node.replace("*", ".*") + ")$");

        for (Map.Entry<String, Boolean> perm : getNodes(player).entrySet()) {
            boolean matches = regex.matcher(perm.getKey()).matches();
            if (!matches) continue;

            if (perm.getValue()) {
                matchesPos = true;
            } else {
                return false;
            }
        }

        return matchesPos;
    }
}
