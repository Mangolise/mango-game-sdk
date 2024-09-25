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

public class TimeSwitcherMenu extends InventoryMenu {
    public static final TimeSwitcherMenu MENU = new TimeSwitcherMenu();

    public TimeSwitcherMenu() {
        super(InventoryType.CHEST_1_ROW, Component.text("Time Switcher").color(TextColor.color(0x119677)));

        setTimeItem(0, Material.CAMPFIRE, "Sunrise", -23000);
        setTimeItem(1, Material.YELLOW_TERRACOTTA, "Morning", 1000);
        setTimeItem(2, Material.SUNFLOWER, "Midday", -6000);
        setTimeItem(3, Material.WHITE_TERRACOTTA, "Afternoon", -10000);
        setTimeItem(5, Material.SOUL_CAMPFIRE, "Sunset", -13000);
        setTimeItem(6, Material.LIGHT_GRAY_CONCRETE, "Early Night", -16000);
        setTimeItem(7, Material.NETHER_STAR, "Midnight", -18000);
        setTimeItem(8, Material.GRAY_CONCRETE, "Late Night", -20000);
    }

    private void setTimeItem(int index, Material material, String name, int time) {
        setMenuItem(index, material, Component.text(name)
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(0x14e3c1))).onLeftClick(e -> setTime(e, name, time));
    }

    private void setTime(MenuItemClickEvent e, String name, int time) {
        long worldAge = e.player().getInstance().getWorldAge();
        Player player = e.player();
        TimeUpdatePacket timeUpdatePacket = new TimeUpdatePacket(worldAge, time);
        player.sendPacket(timeUpdatePacket);
        player.sendActionBar(Component.text("The time has changed to ").color(NamedTextColor.GREEN).append(Component.text(name).color(NamedTextColor.GOLD)));
        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 2f));
    }
}
