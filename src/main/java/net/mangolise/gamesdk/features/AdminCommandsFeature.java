package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.features.commands.FlyCommand;
import net.mangolise.gamesdk.features.commands.GameModeCommand;
import net.mangolise.gamesdk.features.commands.TpCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;

public class AdminCommandsFeature implements Game.Feature<Game> {
    @Override
    public void setup(Context<Game> context) {
        CommandManager commands = MinecraftServer.getCommandManager();
        commands.register(new GameModeCommand());
        commands.register(new FlyCommand());
        commands.register(new TpCommand());
    }
}
