package net.mangolise.gamesdk.entity;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CollidableDisplayBlock extends Entity {
    private final List<ShulkerCollision> shulkers;

    public CollidableDisplayBlock(Instance instance, Block block, Point position, int interpolation) {
        super(EntityType.BLOCK_DISPLAY);

        // Create the visible block
        editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(block);
            meta.setHasNoGravity(true);
            meta.setTransformationInterpolationDuration(interpolation);
            meta.setPosRotInterpolationDuration(interpolation);
        });

        setInstance(instance, position);

        // Create the collision
        List<BoundingBox> collisionShapes = ((ShapeImpl) block.registry().collisionShape()).collisionBoundingBoxes();
        shulkers = new ArrayList<>();

        for (BoundingBox shape : collisionShapes) {
            double size = Math.min(shape.width(), Math.min(shape.height(), shape.depth()));
            for (double x = shape.minX(); x < shape.maxX(); x += size) {
                for (double y = shape.minY(); y < shape.maxY(); y += size) {
                    for (double z = shape.minZ(); z < shape.maxZ(); z += size) {
                        x = Math.min(x + size, shape.maxX()) - size;
                        y = Math.min(y + size, shape.maxY()) - size;
                        z = Math.min(z + size, shape.maxZ()) - size;

                        shulkers.add(createCollisionShulker(new Vec(x, y, z), size, interpolation));
                    }
                }
            }
        }

        // create hitbox after move is finished
        for (ShulkerCollision shulker : shulkers) {
            Point spawnPos = position.add(shulker.offset);
            shulker.vehicle.setInstance(instance, spawnPos);
            shulker.shulker.setInstance(instance, spawnPos);
            shulker.vehicle.addPassenger(shulker.shulker);
        }
    }

    private ShulkerCollision createCollisionShulker(Point pos, double size, int interpolation) {
        LivingEntity shulker = new LivingEntity(EntityType.SHULKER);
        shulker.setInvisible(true);
        shulker.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(size);

        Entity shulkerVehicle = new Entity(EntityType.BLOCK_DISPLAY);
        shulkerVehicle.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.AIR);
            meta.setInvisible(true);
            meta.setHasNoGravity(true);
            meta.setTransformationInterpolationDuration(interpolation);
            meta.setPosRotInterpolationDuration(interpolation);
        });

        return new ShulkerCollision(shulker, shulkerVehicle, pos.add(size / 2d, 0.0, size / 2d));
    }

    @Override
    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, long @Nullable [] chunks, int flags, boolean shouldConfirm) {
        for (ShulkerCollision shulker : shulkers) {
            shulker.vehicle.teleport(position.add(shulker.offset), chunks, flags, shouldConfirm);
        }

        return super.teleport(position, chunks, flags, shouldConfirm);
    }

    private void setInterpolation(int interpolation) {
        editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setTransformationInterpolationDuration(interpolation);
            meta.setPosRotInterpolationDuration(interpolation);
        });

        for (ShulkerCollision shulker : shulkers) {
            shulker.vehicle.editEntityMeta(BlockDisplayMeta.class, meta -> {
                meta.setTransformationInterpolationDuration(interpolation);
                meta.setPosRotInterpolationDuration(interpolation);
            });
        }
    }

    private record ShulkerCollision(LivingEntity shulker, Entity vehicle, Point offset) { }
}
