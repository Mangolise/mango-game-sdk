package net.mangolise.gamesdk;

import net.mangolise.gamesdk.config.ServerConfig;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Taggable;

import java.util.Set;

public interface Game<C> extends Taggable {
    void start(Set<Player> players);

    C config();
}
