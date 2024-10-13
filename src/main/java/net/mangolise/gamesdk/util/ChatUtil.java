package net.mangolise.gamesdk.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ChatUtil {
    private static final char COLOR_CHAR = '&';
    private static final char SPECIAL_CHAR = '§';
    private static final String VALID_COLORS = "0123456789abcdefklmnor";

    /**
     * Converts a legacy & color code string to a modern Adventure Component.
     * @param message The message to convert.
     * @return The converted message.
     */
    public static Component toComponent(String message, Object... args) {
        return toComponent(String.format(message, args));
    }

    /**
     * Converts a legacy & color code string to a modern Adventure Component.
     * @param message The message to convert.
     * @return The converted message.
     */
    public static Component toComponent(String message) {
        Component component = Component.empty();
        StringBuilder currentSection = new StringBuilder();

        NamedTextColor color = NamedTextColor.WHITE;
        TextDecoration decoration = null;
        for (int i = 0; i < message.length(); i++) {
            if (message.charAt(i) != COLOR_CHAR || !VALID_COLORS.contains(String.valueOf(message.charAt(i + 1)))) {
                currentSection.append(message.charAt(i));
                continue;
            }

            // Apply prev section
            if (!currentSection.isEmpty()) {
                component = applyComponent(component, currentSection, color, decoration);
                currentSection = new StringBuilder();
            }

            i++;

            char modifier = message.charAt(i);
            decoration = null;
            switch (modifier) {
                case '0' -> color = NamedTextColor.BLACK;
                case '1' -> color = NamedTextColor.DARK_BLUE;
                case '2' -> color = NamedTextColor.DARK_GREEN;
                case '3' -> color = NamedTextColor.DARK_AQUA;
                case '4' -> color = NamedTextColor.DARK_RED;
                case '5' -> color = NamedTextColor.DARK_PURPLE;
                case '6' -> color = NamedTextColor.GOLD;
                case '7' -> color = NamedTextColor.GRAY;
                case '8' -> color = NamedTextColor.DARK_GRAY;
                case '9' -> color = NamedTextColor.BLUE;
                case 'a' -> color = NamedTextColor.GREEN;
                case 'b' -> color = NamedTextColor.AQUA;
                case 'c' -> color = NamedTextColor.RED;
                case 'd' -> color = NamedTextColor.LIGHT_PURPLE;
                case 'e' -> color = NamedTextColor.YELLOW;
                case 'f', 'r' -> color = NamedTextColor.WHITE;
                case 'k' -> decoration = TextDecoration.OBFUSCATED;
                case 'l' -> decoration = TextDecoration.BOLD;
                case 'm' -> decoration = TextDecoration.STRIKETHROUGH;
                case 'n' -> decoration = TextDecoration.UNDERLINED;
                case 'o' -> decoration = TextDecoration.ITALIC;
            }
        }

        if (!currentSection.isEmpty()) {
            component = applyComponent(component, currentSection, color, decoration);
        }

        return component;
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

        // if time needs hours
        if (time >= 60*60*1000) {
            return String.format("%02d:%02d:%02d.%02d", time / 3600000, time / 60000 % 60, time / 1000 % 60, millis);
        }

        // if time needs minutes
        if (time >= 60*1000) {
            return String.format("%02d:%02d.%02d", time / 60000, time / 1000 % 60, millis);
        }

        return String.format("%02d.%02d", time / 1000, millis);
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
