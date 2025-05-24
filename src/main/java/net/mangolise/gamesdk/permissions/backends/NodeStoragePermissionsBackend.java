package net.mangolise.gamesdk.permissions.backends;

import net.mangolise.gamesdk.permissions.IPermissionsBackend;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.regex.Pattern;

public abstract class NodeStoragePermissionsBackend implements IPermissionsBackend {

    public abstract Map<String, Boolean> getNodes(Player player);  // immutable return
    public abstract void setNodes(Player player, Map<String, Boolean> nodes);

    @Override
    public void setPermission(Player player, String permission, boolean val) {
        Map<String, Boolean> vals = getNodes(player);
        vals.put(permission, val);
        setNodes(player, vals);
    }

    @Override
    public void removePermission(Player player, String permission) {
        Map<String, Boolean> vals = getNodes(player);
        vals.remove(permission);
        setNodes(player, vals);
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
