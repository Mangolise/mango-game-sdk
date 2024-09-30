package net.mangolise.gamesdk.features.commands;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.RelativeVec;

public class FillCommand extends MangoliseCommand {
    @Override
    protected String getPermission() {
        return "mangolise.command.fill";
    }

    public FillCommand() {
        super("fill");
        addPlayerSyntax(this::execute, ArgumentType.RelativeVec3("pos1"), ArgumentType.RelativeVec3("pos2"), ArgumentType.BlockState("block"));
    }

    private void execute(Player player, CommandContext context) {
        Point pos1 = new BlockVec(context.<RelativeVec>get("pos1").fromSender(player));
        Point pos2 = new BlockVec(context.<RelativeVec>get("pos2").fromSender(player));
        Block block = context.get("block");

        Point minPos = new Vec(
                Math.min(pos1.x(), pos2.x()),
                Math.min(pos1.y(), pos2.y()),
                Math.min(pos1.z(), pos2.z())
        );

        Point maxPos = new Vec(
                Math.max(pos1.x(), pos2.x()),
                Math.max(pos1.y(), pos2.y()),
                Math.max(pos1.z(), pos2.z())
        );

        Instance instance = player.getInstance();

        for (double x = minPos.x(); x <= maxPos.x(); x++) {
            for (double y = minPos.y(); y <= maxPos.y(); y++) {
                for (double z = minPos.z(); z <= maxPos.z(); z++) {
                    instance.setBlock(new Vec(x, y, z), block);
                }
            }
        }
    }
}
