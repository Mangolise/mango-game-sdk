package net.mangolise.gamesdk.entity;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CollidableDisplayBlock extends Entity {
    private static final BoundingBox PLAYER_BOUNDING_BOX = new BoundingBox(0.6, 1.8, 0.6, new Vec(-0.3, 0.0, -0.3));

    private final List<ShulkerCollision> shulkers;
    private Point firstOffset;
    private LivingEntity firstShulker;
    private Block block;
    private Collection<BoundingBox> collision;
    private int interpolation;
    private double minimumPlayerScale;
    private Vec scale;

    public static Builder builder(Instance instance, Block block, Point position) {
        return new Builder(instance, block, position);
    }

    public CollidableDisplayBlock(Instance instance, Block block, Point position, int interpolation, Point scale, @Nullable Collection<BoundingBox> customCollision, double minimumPlayerScale) {
        super(EntityType.BLOCK_DISPLAY);
        this.block = block;
        this.interpolation = interpolation;
        this.minimumPlayerScale = minimumPlayerScale;
        this.scale = Vec.fromPoint(scale);

        // Create the visible block
        editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(block);
            meta.setInvisible(true);
            meta.setHasNoGravity(true);
            meta.setTransformationInterpolationDuration(interpolation);
            meta.setPosRotInterpolationDuration(interpolation);
            meta.setScale(this.scale);
        });

        shulkers = new ArrayList<>();
        createShulkerCollision(customCollision, instance, position);
    }

    private void createShulkerCollision(@Nullable Collection<BoundingBox> customCollision, Instance instance, Point position) {
        // Create the collision
        if (customCollision == null) {
            Shape shape = block.registry().collisionShape();
            switch (shape) {
                case BoundingBox box -> this.collision = Collections.singletonList(box);
                case ShapeImpl impl -> this.collision = impl.collisionBoundingBoxes();
                default -> throw new IllegalStateException(shape.getClass().getSimpleName() + " is a supported block collision shape");
            }
        } else {
            this.collision = customCollision;
        }

        if (this.collision.isEmpty()) {
            this.editEntityMeta(BlockDisplayMeta.class, meta -> meta.setTranslation(Vec.ZERO));
            return;
        }

        for (BoundingBox shape : this.collision) {
            // apply scale to shape
            shape = new BoundingBox(shape.width() * scale.x(), shape.height() * scale.y(), shape.depth() * scale.z(), shape.relativeStart().mul(scale));

            double size = Math.min(shape.width(), Math.min(shape.height(), shape.depth()));
            double stepX = PLAYER_BOUNDING_BOX.width() * minimumPlayerScale + size;
            double stepY = PLAYER_BOUNDING_BOX.height() * minimumPlayerScale + size;
            double stepZ = PLAYER_BOUNDING_BOX.depth() * minimumPlayerScale + size;

            double lastX = -1;
            double lastY;
            double lastZ;

            // loops end when it tries to place 2 shulkers in the same position and continues until it runs out
            // of places to place shulkers

            // if (x >= shape.maxX() - size) x = shape.maxX() - size;
            //     make sure it stays inside the bounding box and doesn't go past the outer bounds
            //
            // if (Math.abs(x - lastX) < Vec.EPSILON)
            //     check if the position actually changed this iteration, (above is how it can be the same as last time)

            x: for (double x = shape.minX(); true; x += stepX) {
                if (x >= shape.maxX() - size) x = shape.maxX() - size;

                if (Math.abs(x - lastX) < Vec.EPSILON) {
                    break;
                }

                lastX = x;
                lastY = -1;

                y: for (double y = shape.minY(); true; y += stepY) {
                    if (y >= shape.maxY() - size) y = shape.maxY() - size;

                    if (Math.abs(y - lastY) < Vec.EPSILON) {
                        continue x;
                    }

                    lastY = y;
                    lastZ = -1;

                    for (double z = shape.minZ(); true; z += stepZ) {
                        if (z >= shape.maxZ() - size) z = shape.maxZ() - size;

                        if (Math.abs(z - lastZ) < Vec.EPSILON) {
                            continue y;
                        }

                        lastZ = z;

                        shulkers.add(createCollisionShulker(new Vec(x, y, z), size, interpolation));
                    }
                }
            }
        }

        // Optimisation: first shulker can ride the shulker for displaying the block and we can use a transform offset
        // to display it at the correct position
        {
            ShulkerCollision shulker = shulkers.removeFirst();
            shulker.vehicle.remove();
            firstShulker = shulker.shulker;
            firstOffset = shulker.offset;

            Point spawnPos = position.add(shulker.offset);//.add(0.5, 0.0, 0.5);

            // setInstance runs teleport if the block is already in this instance
            setInstance(instance, spawnPos).thenAccept(ignored -> this.addPassenger(shulker.shulker));

            this.editEntityMeta(BlockDisplayMeta.class, meta -> meta.setTranslation(shulker.offset.mul(-1)));
        }

        // place shulkers in world on top of display block entities so that they don't snap to blocks
        for (ShulkerCollision shulker : shulkers) {
            Point spawnPos = position.add(shulker.offset);

            shulker.vehicle.setInstance(instance, spawnPos).thenAccept(ignored ->
                    shulker.vehicle.addPassenger(shulker.shulker));
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

        return super.teleport(position.add(firstOffset), chunks, flags, shouldConfirm);
    }

    public int getInterpolation() {
        return interpolation;
    }

    private void setInterpolation(int interpolation) {
        this.interpolation = interpolation;

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

    public Collection<BoundingBox> getCollision() {
        return collision;
    }

    public Block getBlock() {
        return block;
    }

    public double getMinimumPlayerScale() {
        return minimumPlayerScale;
    }

    public Vec getScale() {
        return scale;
    }

    public void rebuild(Block block, @Nullable Collection<BoundingBox> customCollision, Point scale, double minimumPlayerScale) {
        this.block = block;
        this.collision = customCollision;
        this.scale = Vec.fromPoint(scale);
        this.minimumPlayerScale = minimumPlayerScale;

        for (ShulkerCollision shulker : shulkers) {
            shulker.shulker.remove();
            shulker.vehicle.remove();
        }

        firstShulker.remove();

        shulkers.clear();
        createShulkerCollision(customCollision, instance, position);
    }

    private record ShulkerCollision(LivingEntity shulker, Entity vehicle, Point offset) { }

    public static class Builder {
        private final Instance instance;
        private final Block block;
        private final Point position;
        private int interpolation;
        private Point scale;
        private @Nullable Collection<BoundingBox> customCollision;
        private double minimumPlayerScale;

        private Builder(Instance instance, Block block, Point position) {
            this.instance = instance;
            this.block = block;
            this.position = position;
            this.interpolation = 0;
            this.scale = Vec.ONE;
            this.customCollision = null;
            this.minimumPlayerScale = 1;
        }

        /**
         * @param interpolation interpolation time in ticks
         */
        public Builder interpolation(int interpolation) {
            this.interpolation = interpolation;
            return this;
        }

        /**
         * @param scale The scale for the display block entity
         */
        public Builder scale(Point scale) {
            this.scale = scale;
            return this;
        }

        /**
         * @param customCollision the collision shape for the block, if null it will use the blocks collision shape
         */
        public Builder customCollision(@Nullable Collection<BoundingBox> customCollision) {
            this.customCollision = customCollision;
            return this;
        }

        /**
         * @param minimumPlayerScale the minimum scale for any player in this game, if you aren't changing the
         *                           players scale attribute, keep this at 1 (default). If you scale the player below
         *                           this number, they might fall/walk through these blocks. can be above 1
         */
        public Builder minimumPlayerScale(double minimumPlayerScale) {
            this.minimumPlayerScale = minimumPlayerScale;
            return this;
        }

        public CollidableDisplayBlock build() {
            return new CollidableDisplayBlock(instance, block, position, interpolation, scale, customCollision, minimumPlayerScale);
        }
    }
}
