package net.mangolise.gamesdk.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ChatUtil {
    private static final char COLOR_CHAR = '&';
    private static final char SPECIAL_CHAR = '§';
    private static final String VALID_COLORS = "0123456789abcdefklmnor";

    /**
     * Converts a legacy & color code string to a modern Adventure Component.
     * @param message The message to convert.
     * @return The converted message.
     */
    public static Component toComponent(String message) {
        Component component = Component.empty();
        StringBuilder currentSection = new StringBuilder();

        NamedTextColor colour = NamedTextColor.WHITE;
        TextDecoration decoration = null;
        for (int i = 0; i < message.length(); i++) {
            if (message.charAt(i) != COLOR_CHAR || !VALID_COLORS.contains(String.valueOf(message.charAt(i + 1)))) {
                currentSection.append(message.charAt(i));
                continue;
            }

            // Apply prev section
            if (!currentSection.isEmpty()) {
                component = component.append(Component.text(currentSection.toString(), colour, decoration));
                currentSection = new StringBuilder();
            }

            char modifier = message.charAt(i + 1);
            switch (modifier) {
                case '0':
                    colour = NamedTextColor.BLACK;
                    break;

                case '1':
                    colour = NamedTextColor.DARK_BLUE;
                    break;

                case '2':
                    colour = NamedTextColor.DARK_GREEN;
                    break;

                case '3':
                    colour = NamedTextColor.DARK_AQUA;
                    break;

                case '4':
                    colour = NamedTextColor.DARK_RED;
                    break;

                case '5':
                    colour = NamedTextColor.DARK_PURPLE;
                    break;

                case '6':
                    colour = NamedTextColor.GOLD;
                    break;

                case '7':
                    colour = NamedTextColor.GRAY;
                    break;

                case '8':
                    colour = NamedTextColor.DARK_GRAY;
                    break;

                case '9':
                    colour = NamedTextColor.BLUE;
                    break;

                case 'a':
                    colour = NamedTextColor.GREEN;
                    break;

                case 'b':
                    colour = NamedTextColor.AQUA;
                    break;

                case 'c':
                    colour = NamedTextColor.RED;
                    break;

                case 'd':
                    colour = NamedTextColor.LIGHT_PURPLE;
                    break;

                case 'e':
                    colour = NamedTextColor.YELLOW;
                    break;

                case 'f':
                    colour = NamedTextColor.WHITE;
                    break;

                case 'k':
                    decoration = TextDecoration.OBFUSCATED;
                    break;

                case 'l':
                    decoration = TextDecoration.BOLD;
                    break;

                case 'm':
                    decoration = TextDecoration.STRIKETHROUGH;
                    break;

                case 'n':
                    decoration = TextDecoration.UNDERLINED;
                    break;

                case 'o':
                    decoration = TextDecoration.ITALIC;
                    break;

                case 'r':
                    decoration = null;
                    break;
            }
        }

        return component;
    }

    public static String colourise(String message) {
        return message.replace('&', SPECIAL_CHAR);
    }
}
