package net.mangolise.gamesdk.event;

import net.minestom.server.entity.Player;

import java.util.Set;

/**
 * Starts when all players have joined the game, and the game is ready to start
 * @param players the players that have joined the game
 */
public record PlayersReadyEvent(Set<Player> players) {
}
