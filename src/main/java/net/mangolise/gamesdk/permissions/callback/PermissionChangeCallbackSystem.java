package net.mangolise.gamesdk.permissions.callback;

import net.mangolise.gamesdk.permissions.PermissionsHelper;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PermissionChangeCallbackSystem {
    private static final List<PermissionChangeListener> listeners = new ArrayList<>();

    public Runnable registerCallback(@Nullable Player player, @Nullable String node, PermissionChangeCallback callback) {
        PermissionChangeListener pcl = new PermissionChangeListener(player, node, callback);
        listeners.add(pcl);
        return () -> listeners.remove(pcl);
    }

    public void runPermissionChangeCallback(Player player, String node) {
        for (PermissionChangeListener listener : listeners) {
            if ((listener.player() == null || listener.player().getUuid().equals(player.getUuid())) &&
                    (listener.node() == null || PermissionsHelper.isNodeSubsetOf(node, listener.node()))) {
                listener.callback().onPermissionChange(player, node);
            }
        }
    }
}
