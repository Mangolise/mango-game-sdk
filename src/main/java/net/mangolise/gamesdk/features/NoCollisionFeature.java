package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;

public class NoCollisionFeature implements Game.Feature<Game> {
    @Override
    public void setup(Context<Game> context) {
        Team team = MinecraftServer.getTeamManager().createTeam("no_collision");
        team.setCollisionRule(TeamsPacket.CollisionRule.NEVER);

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e -> e.getPlayer().setTeam(team));
    }
}
