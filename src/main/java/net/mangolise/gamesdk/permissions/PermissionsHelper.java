package net.mangolise.gamesdk.permissions;

import java.util.regex.Pattern;

public class PermissionsHelper {

    /**
     * Checks if the given permission node is a subset of the provided permission.
     * In other words it checks if the permission node is a more specific version of the permission.
     * <p>
     * For example, perm might be "game.*" and node might be "game.play", in which case this method would return true,
     * indicating that "game.play" is a subset of "game.*".
     * <p>
     * Alternatively, if perm is "game.play" and node is "game.leave", this method would return false,
     * indicating that "game.leave" is not a subset of "game.play".
     * @param perm the permission to check against, which can contain wildcards
     * @param node the permission node to check if it is a subset of the permission
     * @return true if the node is a subset of the permission, false otherwise
     */
    public static boolean isNodeSubsetOf(String perm, String node) {
        Pattern regex = Pattern.compile("^(" + perm.replace("*", ".*") + ")$");
        return regex.matcher(node).matches();
    }
}
