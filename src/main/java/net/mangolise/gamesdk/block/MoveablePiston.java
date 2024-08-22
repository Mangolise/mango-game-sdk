package net.mangolise.gamesdk.block;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.ThreadLocalRandom;

public class MoveablePiston {
    private final Instance instance;
    private final Vec pos;
    private final Vec normal;
    private final String facing;
    private final byte actionParam;
    private boolean open;

    public MoveablePiston(Instance instance, Vec pos, String facing) {
        this.pos = pos;
        this.facing = facing;
        this.open = false;
        this.instance = instance;

        switch (facing) {
            case "north" -> {
                this.normal = new Vec(0, 0, -1);
                this.actionParam = 4;
            }

            case "south" -> {
                this.normal = new Vec(0, 0, 1);
                this.actionParam = 2;
            }

            case "east" -> {
                this.normal = new Vec(1, 0, 0);
                this.actionParam = 5;
            }

            case "west" -> {
                this.normal = new Vec(-1, 0, 0);
                this.actionParam = 3;
            }

            case "up" -> {
                this.normal = new Vec(0, 1, 0);
                this.actionParam = 1;
            }

            case "down" -> {
                this.normal = new Vec(0, -1, 0);
                this.actionParam = 0;
            }

            default -> throw new IllegalArgumentException("piston invalid facing dir");
        }
    }

    public void open() {
        if (open) {
            return;
        }

        ServerPacket packet = new BlockActionPacket(pos, (byte)0, actionParam, Block.STICKY_PISTON);
        instance.sendGroupedPacket(packet);
        instance.playSound(Sound.sound(SoundEvent.BLOCK_PISTON_EXTEND, Sound.Source.BLOCK, 0.5f,
                ThreadLocalRandom.current().nextFloat(0.6f, 0.85f)), pos);

        instance.scheduler().scheduleTask(() -> {
            instance.setBlock(pos.add(normal), Block.PISTON_HEAD
                    .withProperty("facing", facing)
                    .withProperty("type", "sticky"));
            instance.setBlock(pos.add(normal.mul(2)), Block.SLIME_BLOCK);
            return TaskSchedule.stop();
        }, TaskSchedule.tick(3));

        open = true;
    }

    public void close() {
        if (!open) {
            return;
        }

        ServerPacket packet = new BlockActionPacket(pos, (byte)1, actionParam, Block.STICKY_PISTON);
        instance.sendGroupedPacket(packet);
        instance.playSound(Sound.sound(SoundEvent.BLOCK_PISTON_CONTRACT, Sound.Source.BLOCK, 0.5f,
                ThreadLocalRandom.current().nextFloat(0.6f, 0.8f)), pos);

        instance.scheduler().scheduleTask(() -> {
            instance.setBlock(pos.add(normal), Block.SLIME_BLOCK);
            instance.setBlock(pos.add(normal.mul(2)), Block.AIR);
            return TaskSchedule.stop();
        }, TaskSchedule.tick(3));

        open = false;
    }

    public Vec getPos() {
        return pos;
    }

    public Vec getNormal() {
        return normal;
    }

    public String getFacing() {
        return facing;
    }

    public boolean isOpen() {
        return open;
    }
}
