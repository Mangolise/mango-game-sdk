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

public class TimeSwitcherMenu extends InventoryMenu {
    public static final TimeSwitcherMenu MENU = new TimeSwitcherMenu();
    private static final Tag<Integer> SELECTED_TIME_TAG = Tag.Integer("gamesdk_selected_time").defaultValue(-26000);
    private static final Tag<String> SELECTED_TIME_NAME_TAG = Tag.String("gamesdk_selected_time_name").defaultValue("Morning");

    public TimeSwitcherMenu() {
        super(InventoryType.CHEST_1_ROW, Component.text("Time Switcher").color(TextColor.color(0x119677)));

        setTimeItem(0, Material.CAMPFIRE, "Sunrise", -23000, 23000);
        setTimeItem(1, Material.YELLOW_TERRACOTTA, "Morning", -26000, 26000);
        setTimeItem(2, Material.SUNFLOWER, "Midday", -6000, 6000);
        setTimeItem(3, Material.WHITE_TERRACOTTA, "Afternoon", -10000, 10000);
        setTimeItem(5, Material.SOUL_CAMPFIRE, "Sunset", -13000, 13000);
        setTimeItem(6, Material.LIGHT_GRAY_CONCRETE, "Early Night", -15200, 15200);
        setTimeItem(7, Material.NETHER_STAR, "Midnight", -18000, 18000);
        setTimeItem(8, Material.GRAY_CONCRETE, "Late Night", -20000, 20000);

        setMenuItem(4, Material.CLOCK, decorateText("Toggle Time Changing")).onLeftClick(e -> {
            Player player = e.player();
            int time = player.updateAndGetTag(SELECTED_TIME_TAG, t -> -t);
            setTime(e, player.getTag(SELECTED_TIME_NAME_TAG), time, time);
        });
    }

    private void setTimeItem(int index, Material material, String name, int time, int movingTime) {
        setMenuItem(index, material, decorateText(name)).onLeftClick(e -> setTime(e, name, time, movingTime));
    }

    private Component decorateText(String text) {
        return Component.text(text)
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(0x14e3c1));
    }

    private void setTime(MenuItemClickEvent e, String name, int time, int movingTime) {
        long worldAge = e.player().getInstance().getWorldAge();
        Player player = e.player();

        // make it moving time if player has moving time enabled
        if (player.getTag(SELECTED_TIME_TAG) >= 0) {
            time = movingTime;
        }

        player.setTag(SELECTED_TIME_TAG, time);
        player.setTag(SELECTED_TIME_NAME_TAG, name);

        TimeUpdatePacket timeUpdatePacket = new TimeUpdatePacket(worldAge, time, false);
        player.sendPacket(timeUpdatePacket);
        player.sendActionBar(Component.text("The time has changed to ").color(NamedTextColor.GREEN).append(Component.text(name).color(NamedTextColor.GOLD)));
        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 2f));
    }
}
