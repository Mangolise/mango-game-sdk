package net.mangolise.gamesdk;

import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;

public abstract class BaseGame<C extends Record> implements Game {

    protected TagHandler tagHandler = TagHandler.newHandler();

    protected final C config;
    protected BaseGame(C config) {
        this.config = config;
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    public @NotNull C config() {
        return config;
    }
}
