package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.events.PlayerAttemptDismountEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket;

public class ExtraEventsFeature implements Game.Feature<Game> {

    @Override
    public void setup(Context<Game> context) {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketEvent.class, e -> {
            if (!(e.getPacket() instanceof ClientSteerVehiclePacket packet)) return;
            if (packet.flags() == 2) {
                if (e.getPlayer().getVehicle() == null) return;
                PlayerAttemptDismountEvent event = new PlayerAttemptDismountEvent(e.getPlayer(), e.getPlayer().getVehicle());
                MinecraftServer.getGlobalEventHandler().call(event);
            }
        });
    }
}
