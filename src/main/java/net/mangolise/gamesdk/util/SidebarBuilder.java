package net.mangolise.gamesdk.util;

import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Sidebar;

import java.util.ArrayList;
import java.util.List;

public class SidebarBuilder {
    private final List<Component> lines = new ArrayList<>();

    public SidebarBuilder addLine(Component line) {
        lines.add(line);
        return this;
    }

    public SidebarBuilder addLine(String line) {
        lines.add(Component.text(line));
        return this;
    }

    public Sidebar build(Component title) {
        Sidebar sidebar = new Sidebar(title);
        apply(sidebar);

        return sidebar;
    }

    public Sidebar build(String title) {
        return build(Component.text(title));
    }

    public void apply(Sidebar sidebar) {
        sidebar.getLines().forEach(line -> sidebar.removeLine(line.getId()));

        for (int i = 0; i < lines.size(); i++) {
            sidebar.createLine(new Sidebar.ScoreboardLine(String.valueOf(i), lines.get(i), lines.size() - i - 1, Sidebar.NumberFormat.blank()));
        }
    }
}
