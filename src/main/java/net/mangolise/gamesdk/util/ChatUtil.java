package net.mangolise.gamesdk.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ChatUtil {

    /**
     * Converts a legacy & color code string to a modern Adventure Component.
     * @param message The message to convert.
     * @return The converted message.
     */
    public static Component toComponent(String message, Object... args) {
        message = String.format(message, args);
        return LegacyComponentSerializer.legacy('&').deserialize(message);
    }

    public static @Nullable NamedTextColor codeToNamedTextColor(String code) {
        code = code.replace("§", "").replace("&", "");
        return switch (code) {
            case "0" -> NamedTextColor.BLACK;
            case "1" -> NamedTextColor.DARK_BLUE;
            case "2" -> NamedTextColor.DARK_GREEN;
            case "3" -> NamedTextColor.DARK_AQUA;
            case "4" -> NamedTextColor.DARK_RED;
            case "5" -> NamedTextColor.DARK_PURPLE;
            case "6" -> NamedTextColor.GOLD;
            case "7" -> NamedTextColor.GRAY;
            case "8" -> NamedTextColor.DARK_GRAY;
            case "9" -> NamedTextColor.BLUE;
            case "a" -> NamedTextColor.GREEN;
            case "b" -> NamedTextColor.AQUA;
            case "c" -> NamedTextColor.RED;
            case "d" -> NamedTextColor.LIGHT_PURPLE;
            case "e" -> NamedTextColor.YELLOW;
            default -> null;
        };
    }

    private static Component applyComponent(Component component, StringBuilder currentSection, NamedTextColor color, TextDecoration decoration) {
        if (decoration == null) {
            return component.append(Component.text(currentSection.toString(), color));
        } else {
            return component.append(Component.text(currentSection.toString(), color, decoration));
        }
    }

    public static Component getDisplayName(Player player) {
        if (player.getDisplayName() == null) {
            return Component.text(player.getUsername(), NamedTextColor.WHITE);
        } else {
            return player.getDisplayName();
        }
    }

    /**
     * Converts a millisecond time to a human-readable timer
     *
     * @param time the time in milliseconds
     * @return the formatted time
     */
    public static String formatTime(long time) {
        return formatTime(time, true);
    }

    /**
     * Converts a millisecond time to a human-readable timer
     *
     * @param time the time in milliseconds
     * @param exact whether to round to the nearest tick (0.05 seconds)
     * @return the formatted time
     */
    public static String formatTime(long time, boolean exact) {
        long millis = (exact ? (time / 10) : (time / 50 * 5)) % 100;

        // if milliseconds
        if (time > 60*60*1000) {
            return String.format("%02d:%02d:%02d.%02d", time / 3600000, time / 60000 % 60, time / 1000 % 60, millis);
        }

        return String.format("%02d:%02d.%02d", time / 60000, time / 1000 % 60, millis);
    }

    /**
     * Converts a snake_case string to a Title Case string
     * @param snakeCase The snake_case string
     * @return The Title Case string
     */
    public static String snakeCaseToTitleCase(String snakeCase) {
        if (snakeCase.isEmpty()) {
            return "";
        }

        StringBuilder cString = new StringBuilder(snakeCase.toLowerCase());
        if (cString.charAt(0) != '_') {
            cString.setCharAt(0, Character.toUpperCase(cString.charAt(0)));
        }

        int cIndex = cString.indexOf("_");
        while (cIndex != -1) {
            cString.setCharAt(cIndex, ' ');
            cString.setCharAt(cIndex + 1, Character.toUpperCase(cString.charAt(cIndex + 1)));
            cIndex = cString.indexOf("_");
        }
        return cString.toString();
    }

    /**
     * Capitalises the first letter of a string
     *
     * @param string The string to capitalise the first letter of
     * @return The string with the first letter capitalised
     */
    public static String capitaliseFirstLetter(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
    }
}
