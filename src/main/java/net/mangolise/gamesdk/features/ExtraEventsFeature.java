package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.events.PlayerAttemptDismountEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.network.packet.client.play.ClientInputPacket;

public class ExtraEventsFeature implements Game.Feature<Game> {

    @Override
    public void setup(Context<Game> context) {
        context.eventNode().addListener(PlayerPacketEvent.class, e -> {
            if (!(e.getPacket() instanceof ClientInputPacket(byte flags))) return;
            if ((flags & 0x20) != 0) {
                if (e.getPlayer().getVehicle() == null) return;
                PlayerAttemptDismountEvent event = new PlayerAttemptDismountEvent(e.getPlayer(), e.getPlayer().getVehicle());
                context.eventNode().call(event);
            }
        });
    }
}
