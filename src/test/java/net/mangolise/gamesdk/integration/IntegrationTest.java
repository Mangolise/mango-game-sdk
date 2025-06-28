package net.mangolise.gamesdk.integration;

import net.mangolise.gamesdk.permissions.Permissions;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.bungee.BungeeCordProxy;

public class IntegrationTest {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        if (GameSdkUtils.useBungeeCord()) {
            BungeeCordProxy.enable();
        }

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, e -> {
            e.getPlayer().setPermissionLevel(4);
            Permissions.setPermission(e.getPlayer(), "*", true);
        });

        IntegrationGame game = new IntegrationGame(new IntegrationGame.Config());
        game.setup();

        server.start("0.0.0.0", GameSdkUtils.getConfiguredPort());
    }
}
