package net.mangolise.gamesdk.packaging;

import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarWorld;
import net.kyori.adventure.nbt.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.InstanceContainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Packages entities. */
public class EntityPackaging {

    public static void unpackEntities(InstanceContainer instance) {
        if (!(instance.getChunkLoader() instanceof PolarLoader loader)) return;
        PolarWorld world = loader.world();
        byte[] data = world.userData();

        Map<Entity, Point> entities = unpackEntities(data);
        entities.forEach((entity, point) -> entity.setInstance(instance, point));
    }

    public static Map<Entity, Point> unpackEntities(byte[] data) {
        Map<Entity, Point> entities = new HashMap<>();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            while (bais.available() > 0) {
                CompoundBinaryTag tag = BinaryTagIO.reader().read(bais);
                for (var tags : tag) {
                    CompoundBinaryTag entityTag = (CompoundBinaryTag) tags.getValue();
                    System.out.println(entityTag);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return entities;
    }

    public static byte[] unpackAnvil(Path path) {
        Path entitiesFolder = path.resolve("entities");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // read all entity files
        try {
            Files.walk(entitiesFolder)
                    .filter(Files::isRegularFile)
                    .forEach(entityFile -> {
                        try {
                            byte[] bytes = Files.readAllBytes(entityFile);
                            unpackMcaFileEntities(bytes, baos);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }

    private static void unpackMcaFileEntities(byte[] fileBytes, OutputStream out) throws IOException {
        CompoundBinaryTag tag = BinaryTagIO.reader().read(new ByteArrayInputStream(fileBytes));

        for (var tags : tag) {
            String name = tags.getKey();
            CompoundBinaryTag value = (CompoundBinaryTag) tags.getValue();
            ListBinaryTag list = value.getList("Entities");

            for (BinaryTag binTag : list) {
                CompoundBinaryTag entityTag = (CompoundBinaryTag) binTag;
                BinaryTagIO.writer().write(entityTag, out);
            }
        }
    }
}
