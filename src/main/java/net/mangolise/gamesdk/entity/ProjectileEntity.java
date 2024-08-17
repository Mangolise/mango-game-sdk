package net.mangolise.gamesdk.entity;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.block.Block;

public class ProjectileEntity extends Entity {
    public ProjectileEntity(Block display, double scale) {
        super(EntityType.BLOCK_DISPLAY);
        this.setBoundingBox(new BoundingBox(scale, scale, scale));
        this.editEntityMeta(BlockDisplayMeta.class, meta -> meta.setBlockState(display));
    }
}
