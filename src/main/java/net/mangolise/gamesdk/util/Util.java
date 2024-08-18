package net.mangolise.gamesdk.util;

import java.util.UUID;

public class Util {

    /**
     * Creates a fake uuid for an offline player
     */
    public static UUID createFakeUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer: " + name).getBytes());
    }

    public static int getConfiguredPort() {
        return Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "25565"));
    }
}
