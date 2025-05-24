package net.mangolise.gamesdk.permissions.callback;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

public record PermissionChangeListener(@Nullable Player player, @Nullable String node, PermissionChangeCallback callback) {
}
