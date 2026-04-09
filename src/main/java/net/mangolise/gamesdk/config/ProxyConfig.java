package net.mangolise.gamesdk.config;

import net.mangolise.gamesdk.log.Log;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public sealed interface ProxyConfig {
    record Bungeecord(@Nullable Set<String> guardTokens) implements ProxyConfig { }
    record Velocity(String secret) implements ProxyConfig { }
    record None() implements ProxyConfig { }

    static ProxyConfig createFromEnv() {
        String proxy = System.getenv("PROXY");
        if (proxy == null) {
            return new None();
        }
        switch (proxy) {
            case "bungeecord" -> {
                Set<String> guardTokens = Set.of(System.getenv("BUNGEE_GUARD_TOKENS").split(","));
                return new Bungeecord(guardTokens);
            }
            case "velocity" -> {
                String secret = System.getenv("VELOCITY_SECRET");
                return new Velocity(secret);
            }
            default -> {
                Log.logger().error("Unknown proxy type: {}", proxy);
                return new None();
            }
        }
    }

    static MinecraftServer createServer() {
        var config = ProxyConfig.createFromEnv();
        switch (config) {
            case Bungeecord bungeecord -> {
                MinecraftServer server = MinecraftServer.init(new Auth.Bungee(bungeecord.guardTokens()));
                Log.logger().info("Bungeecord proxy enabled, with {} guard tokens",
                        bungeecord.guardTokens() != null ? bungeecord.guardTokens().size() : 0);
                return server;
            }
            case Velocity velocity -> {
                MinecraftServer server = MinecraftServer.init(new Auth.Velocity(velocity.secret()));
                System.out.println("Velocity proxy enabled with secret: " + velocity.secret());
                return server;
            }
            case None ignored -> {
                return MinecraftServer.init();
            }
            default -> throw new IllegalStateException("Unexpected value: " + config.getClass().getName());
        }
    }
}
