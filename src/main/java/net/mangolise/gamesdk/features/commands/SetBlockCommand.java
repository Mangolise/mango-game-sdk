package net.mangolise.gamesdk.features.commands;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.RelativeVec;

public class SetBlockCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.setblock";
    }

    public SetBlockCommand() {
        super("setblock");

        addPlayerSyntax(this::execute, ArgumentType.BlockState("block"), ArgumentType.RelativeVec3("pos")
                .setDefaultValue(CURRENT_POS_RELATIVEVEC));
    }

    private void execute(Player player, CommandContext context) {
        Block block = context.get("block");
        Vec position = context.<RelativeVec>get("pos").fromSender(player);

        player.getInstance().setBlock(position, block);
    }
}
