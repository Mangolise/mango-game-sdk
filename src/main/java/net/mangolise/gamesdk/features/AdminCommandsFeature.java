package net.mangolise.gamesdk.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.features.commands.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.GameMode;

public class AdminCommandsFeature implements Game.Feature<Game> {
    @Override
    public void setup(Context<Game> context) {
        CommandManager commands = MinecraftServer.getCommandManager();
        commands.register(new GameModeCommand());
        commands.register(new ShortGameModeCommand("gmc", GameMode.CREATIVE));
        commands.register(new ShortGameModeCommand("gms", GameMode.SURVIVAL));
        commands.register(new ShortGameModeCommand("gma", GameMode.ADVENTURE));
        commands.register(new ShortGameModeCommand("gmsp", GameMode.SPECTATOR));
        commands.register(new FlyCommand());
        commands.register(new TpCommand());
        commands.register(new PlaySoundCommand());
        commands.register(new AttributeCommand());
        commands.register(new TpsCommand());
        commands.register(new SetBlockCommand());
        commands.register(new HealCommand());
        commands.register(new FillCommand());
        commands.register(new PacketDebugCommand());
        commands.register(new GiveCommand());
        commands.register(new SummonCommand());
        commands.register(new KillCommand());
    }
}
