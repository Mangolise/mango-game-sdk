package net.mangolise.gamesdk.permissions;

import net.mangolise.gamesdk.permissions.backends.MapPermissionsBackend;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.GameProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class PermissionsTest {

    @Test
    public void testPermissions() {
        Permissions.setBackend(new MapPermissionsBackend());
        MinecraftServer.init();

        // Connection is null for unit test
        @SuppressWarnings("DataFlowIssue") Player player = new Player(null, new GameProfile(UUID.randomUUID(), "PotatoMan"));
        Permissions.setPermission(player, "lovespotatoes", true);
        Permissions.setPermission(player, "lovespotatoes.baked", true);
        Permissions.setPermission(player, "adam.is.*", true);
        Permissions.setPermission(player, "lovespotatoes.fromcountry.*", true);
        Permissions.setPermission(player, "lovespotatoes.fromcountry.france", false);
        Permissions.setPermission(player, "lovespotatoes.fromcountry.fr*nce", false);

        Assertions.assertTrue(Permissions.hasPermission(player, "lovespotatoes"));
        Assertions.assertTrue(Permissions.hasPermission(player, "lovespotatoes.baked"));
        Assertions.assertTrue(Permissions.hasPermission(player, "adam.is.mad"));
        Assertions.assertFalse(Permissions.hasPermission(player, "lovespotatoes.mashed"));
        Assertions.assertFalse(Permissions.hasPermission(player, "lovespotatoes.fromcountry.france"));
        Assertions.assertFalse(Permissions.hasPermission(player, "lovespotatoes.fromcountry.frHELLOWORLDnce"));

        Permissions.setPermission(player, "*", true);
        Assertions.assertTrue(Permissions.hasPermission(player, "michael.exists"));
    }

    @Test
    public void callbacks() {
        Permissions.setBackend(new MapPermissionsBackend());
        MinecraftServer.init();

        // Connection is null for unit test
        @SuppressWarnings("DataFlowIssue") Player player = new Player(null, new GameProfile(UUID.randomUUID(), "PotatoMan"));
        boolean[] callbackCalled = {false};

        // Setting exact to true
        Permissions.registerCallback(player, "me.lovespotatoes", (p, node) -> callbackCalled[0] = true);
        Permissions.setPermission(player, "me.lovespotatoes", true);
        Assertions.assertTrue(callbackCalled[0]);

        // Setting exact to false
        callbackCalled[0] = false;
        Permissions.removePermission(player, "me.lovespotatoes");
        Assertions.assertTrue(callbackCalled[0]);

        // Setting wildcard to true
        callbackCalled[0] = false;
        Permissions.setPermission(player, "me.*", true);
        Assertions.assertTrue(callbackCalled[0]);

        // Canceling wildcard callback
        callbackCalled[0] = false;
        Permissions.registerCallback(player, "tomato.potato", (p, node) -> callbackCalled[0] = true).run();
        Permissions.setPermission(player, "tomato.*", true);
        Assertions.assertFalse(callbackCalled[0]);

        // Callback for any node
        Permissions.registerCallback(player, null, (p, node) -> callbackCalled[0] = true);
        Permissions.setPermission(player, "any.node", true);
        Assertions.assertTrue(callbackCalled[0]);

        @SuppressWarnings("DataFlowIssue") Player player2 = new Player(null, new GameProfile(UUID.randomUUID(), "PotatoMan2"));

        // Callback for any player and a specific node
        callbackCalled[0] = false;
        Permissions.registerCallback(null, "any.node", (p, node) -> callbackCalled[0] = true);

        // p1
        Permissions.setPermission(player2, "any.node", true);
        Assertions.assertTrue(callbackCalled[0]);

        // p2
        callbackCalled[0] = false;
        Permissions.setPermission(player, "any.node", true);
        Assertions.assertTrue(callbackCalled[0]);

        // Callback for any player and any node
        callbackCalled[0] = false;
        Permissions.registerCallback(null, null, (p, node) -> callbackCalled[0] = true);
        Permissions.setPermission(player2, "some.thing", true);
        Assertions.assertTrue(callbackCalled[0]);
    }
}
