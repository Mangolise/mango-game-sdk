package net.mangolise.gamesdk.permissions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PermissionsHelperTest {

    @Test
    public void subsetTest() {
        assertSubset("game.*", "game.kitpvp");
        assertSubset("game.kitpvp", "game.kitpvp");
        assertSubset("game.kitpvp.*", "game.kitpvp.join");
        assertSubset("game", "game");

        assertNotSubset("game.*", "game");
        assertNotSubset("game.kitpvp", "game.kitpvp.join");
        assertNotSubset("game", "potatos");
    }

    // perm can have wildcards
    private void assertSubset(String perm, String node) {
        Assertions.assertTrue(PermissionsHelper.isNodeSubsetOf(perm, node));
    }
    private void assertNotSubset(String perm, String node) {
        Assertions.assertFalse(PermissionsHelper.isNodeSubsetOf(perm, node));
    }

}
