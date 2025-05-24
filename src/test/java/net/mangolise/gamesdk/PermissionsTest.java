package net.mangolise.gamesdk;

import net.mangolise.gamesdk.permissions.Permissions;
import net.mangolise.gamesdk.permissions.backends.MapPermissionsBackend;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class PermissionsTest {

    @Test
    public void testPermissions() {
        Permissions.setBackend(new MapPermissionsBackend());

        MinecraftServer.init();

        // Connection is null for unit test
        @SuppressWarnings("DataFlowIssue") Player player = new Player(UUID.randomUUID(), "PotatoMan", null);
        Permissions.setPermission(player, "lovespotatoes", true);
        Permissions.setPermission(player, "lovespotatoes.baked", true);
        Permissions.setPermission(player, "lovespotatoes.fromcountry.*", true);
        Permissions.setPermission(player, "lovespotatoes.fromcountry.france", false);
        Permissions.setPermission(player, "lovespotatoes.fromcountry.fr*nce", false);

        Assertions.assertTrue(Permissions.hasPermission(player, "lovespotatoes"));
        Assertions.assertTrue(Permissions.hasPermission(player, "lovespotatoes.baked"));
        Assertions.assertFalse(Permissions.hasPermission(player, "lovespotatoes.mashed"));
        Assertions.assertFalse(Permissions.hasPermission(player, "lovespotatoes.fromcountry.france"));
        Assertions.assertFalse(Permissions.hasPermission(player, "lovespotatoes.fromcountry.frHELLOWORLDnce"));
    }
}
