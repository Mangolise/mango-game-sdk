package net.mangolise.gamesdk.menu;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.util.InventoryMenu;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.TimeUpdatePacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;

import java.sql.Time;
import java.util.List;

public class TimeSwitcherMenu extends InventoryMenu {
    private static final List<TimeSelection> times = List.of(
            new TimeSelection(23000, "Sunrise", Material.CAMPFIRE),
            new TimeSelection(26000, "Morning", Material.YELLOW_TERRACOTTA),
            new TimeSelection(6000, "Midday", Material.SUNFLOWER),
            new TimeSelection(10000, "Afternoon", Material.WHITE_TERRACOTTA),
            new TimeSelection(13000, "Sunset", Material.SOUL_CAMPFIRE),
            new TimeSelection(15200, "Early Night", Material.LIGHT_GRAY_CONCRETE),
            new TimeSelection(18000, "Midnight", Material.NETHER_STAR),
            new TimeSelection(20000, "Late Night", Material.GRAY_CONCRETE)
    );

    private static final Tag<Integer> SELECTED_TIME = Tag.Integer("gamesdk_selected_time").defaultValue(times.get(1).time());
    private static final Tag<Boolean> SELECTED_TIME_MOVES = Tag.Boolean("gamesdk_selected_time_moves").defaultValue(false);
    private static final Tag<Long> SELECTED_TIME_TIME = Tag.Long("gamesdk_selected_time_time");

    public static final TimeSwitcherMenu MENU = new TimeSwitcherMenu();

    public TimeSwitcherMenu() {
        super(InventoryType.CHEST_1_ROW, Component.text("Time Switcher").color(TextColor.color(0x119677)));

        for (int i = 0; i < times.size(); i++) {
            addTimeItem(times.get(i), i);
        }

        addMenuItem(Material.CLOCK, decorateText("Toggle Time Changing")).onLeftClick(e -> {
            Player player = e.player();
            boolean timeMoves = player.updateAndGetTag(SELECTED_TIME_MOVES, t -> !t);
            TimeSelection selection;
            int selectedTime = player.getTag(SELECTED_TIME);

            // time was previously not moving
            if (timeMoves) {
                selection = new TimeSelection(selectedTime, "Changes", null);
            } else {
                // calculate how long it has been and adjust when the time is on the clients perspective
                // this variable is in ticks
                long timeSinceLastChange = 0;
                if (player.hasTag(SELECTED_TIME_TIME)) {
                    timeSinceLastChange = (System.currentTimeMillis() - player.getTag(SELECTED_TIME_TIME)) / 50;
                }

                selection = new TimeSelection(selectedTime + (int)timeSinceLastChange, "Doesn't change", null);
            }

            setTime(player, "Time now ", selection, timeMoves);
        });
    }

    private void addTimeItem(TimeSelection time, int index) {
        int slotIndex = index >= 4 ? index + 1 : index;
        setMenuItem(slotIndex, time.material(), decorateText(time.name())).onLeftClick(e -> {
            Player player = e.player();
            setTime(player, "The time has changed to ", time, player.getTag(SELECTED_TIME_MOVES));
        });
    }

    private Component decorateText(String text) {
        return Component.text(text)
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(0x14e3c1));
    }

    private void setTime(Player player, String startingMessage, TimeSelection time, boolean timeMoves) {
        player.setTag(SELECTED_TIME, time.time());
        player.setTag(SELECTED_TIME_TIME, System.currentTimeMillis());

        long worldAge = player.getInstance().getWorldAge();

        TimeUpdatePacket timeUpdatePacket = new TimeUpdatePacket(worldAge, time.time(), timeMoves);
        player.sendPacket(timeUpdatePacket);
        player.sendActionBar(Component.text(startingMessage).color(NamedTextColor.GREEN).append(Component.text(time.name()).color(NamedTextColor.GOLD)));
        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 2f));
    }
    
    private record TimeSelection(int time, String name, Material material) {}
}
