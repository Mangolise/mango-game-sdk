package net.mangolise.gamesdk.integration;

import net.mangolise.gamesdk.permissions.Permissions;
import net.mangolise.gamesdk.tablist.CustomTabList;
import net.mangolise.gamesdk.tablist.TabListEntry;
import net.mangolise.gamesdk.util.ChatUtil;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;

import java.util.List;

public class IntegrationTest {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        if (GameSdkUtils.useBungeeCord()) {
            BungeeCordProxy.enable();
        }

        MojangAuth.init();

        CustomTabList tabList = new CustomTabList();
        tabList.setHeader(ChatUtil.toComponent("&6&lPOTATO"));
        tabList.setEntriesProvider(p -> {
            return List.of(
                    TabListEntry.text(ChatUtil.toComponent("&aHello")),
                    TabListEntry.text(ChatUtil.toComponent("&bThere")),
                    TabListEntry.text(ChatUtil.toComponent("&c" + p.getUsername())),
                    TabListEntry.text(ChatUtil.toComponent("Online: " + MinecraftServer.getConnectionManager().getOnlinePlayerCount()))
            );
        });

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, e -> {
            e.getPlayer().setPermissionLevel(4);
            Permissions.setPermission(e.getPlayer(), "*", true);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e -> {
            tabList.addPlayer(e.getPlayer());
            e.getPlayer().sendMessage("added to tab list");
            tabList.update();
        });

        IntegrationGame game = new IntegrationGame(new IntegrationGame.Config());
        game.setup();

        server.start("0.0.0.0", GameSdkUtils.getConfiguredPort());
    }
}
