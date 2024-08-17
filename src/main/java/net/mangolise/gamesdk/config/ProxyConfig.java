package net.mangolise.gamesdk.config;

import net.mangolise.gamesdk.log.Log;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
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

    static void apply() {
        switch (ProxyConfig.createFromEnv()) {
            case Bungeecord bungeecord -> {
                BungeeCordProxy.enable();
                Log.logger().info("Bungeecord proxy enabled");
                if (bungeecord.guardTokens() != null) {
                    Log.logger().info("Setting {} Bungeecord guard tokens", bungeecord.guardTokens().size());
                    BungeeCordProxy.setBungeeGuardTokens(bungeecord.guardTokens());
                }
            }
            case Velocity velocity -> {
                VelocityProxy.enable(velocity.secret());
                System.out.println("Velocity proxy enabled with secret: " + velocity.secret());
            }
            case None ignored -> {}
        }
    }
}
