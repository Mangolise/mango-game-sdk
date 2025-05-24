package net.mangolise.gamesdk.permissions.callback;

import net.mangolise.gamesdk.permissions.PermissionsHelper;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing permission change callbacks.
 * This class allows you to register callbacks that will be
 * sorted and called appropriately when {@link #runPermissionChangeCallback(Player, String)} is called.
 */
public class PermissionChangeCallbackSystem {
    private static final List<PermissionChangeListener> listeners = new ArrayList<>();

    /**
     * Registers a callback for permission changes.
     * @param player the player to register the callback for, or null for all players
     * @param node the permission node to register the callback for, or null for all nodes
     * @param callback the callback to register
     * @return a Runnable that can be used to unregister the callback
     */
    public Runnable registerCallback(@Nullable Player player, @Nullable String node, PermissionChangeCallback callback) {
        PermissionChangeListener pcl = new PermissionChangeListener(player, node, callback);
        listeners.add(pcl);
        return () -> listeners.remove(pcl);
    }

    /**
     * Runs the permission change callbacks for a specific player and node.
     * @param player the player for whom the callbacks should be run
     * @param node the permission node that has changed
     */
    public void runPermissionChangeCallback(@NotNull Player player, @NotNull String node) {
        for (PermissionChangeListener listener : listeners) {
            if ((listener.player() == null || listener.player().getUuid().equals(player.getUuid())) &&
                    (listener.node() == null || PermissionsHelper.isNodeSubsetOf(node, listener.node()))) {
                listener.callback().onPermissionChange(player, node);
            }
        }
    }

    /**
     * Represents a listener for permission changes.
     * @param player the player for whom the callback is registered, or null for all players
     * @param node the permission node to listen for changes, or null for all nodes
     * @param callback the callback to invoke when a permission change occurs
     */
    record PermissionChangeListener(@Nullable Player player, @Nullable String node, PermissionChangeCallback callback) { }
}
