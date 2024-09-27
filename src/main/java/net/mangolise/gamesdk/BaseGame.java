package net.mangolise.gamesdk;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public non-sealed abstract class BaseGame<C extends Record> implements Game {

    protected TagHandler tagHandler = TagHandler.newHandler();
    protected final EventNode<Event> eventNode = EventNode.all("game-events");

    protected final C config;
    protected BaseGame(C config) {
        this.config = config;
    }

    protected final List<Runnable> cleanupTasks = new CopyOnWriteArrayList<>();
    private FeatureContext featureContext;

    @Override
    public void setup() {
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);

        // setup all the features
        featureContext = new FeatureContext();

        for (Feature<?> feature : features()) {
            //noinspection unchecked
            feature.setup(featureContext);
            featureContext.loadedFeatures.put(feature.getClass(), feature);
        }

        cleanupTasks.addAll(featureContext.cleanupTasks);

        // register the cleanup tasks
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Runnable cleanupTask : cleanupTasks) {
                cleanupTask.run();
            }
            MinecraftServer.getGlobalEventHandler().removeChild(eventNode);
        }));
    }

    @SuppressWarnings("rawtypes") // dealing with the generics doesn't make sense here, since we know this is the instance of the class
    private class FeatureContext implements Feature.Context {
        @Override
        public Game game() {
            return BaseGame.this;
        }

        private final Map<Class, Feature> loadedFeatures = new ConcurrentHashMap<>();
        @Override
        public Feature feature(Class feature) {
            Feature loaded = loadedFeatures.get(feature);
            if (loaded == null) {
                throw new IllegalStateException("Feature " + feature + " is not loaded");
            }
            return loaded;
        }

        private final List<Runnable> cleanupTasks = new ArrayList<>();
        @Override
        public void cleanup(Runnable cleanupTask) {
            cleanupTasks.add(cleanupTask);
        }
    }

    /**
     * Gets this game's instance of the specified feature.
     * @param clazz The feature class
     * @return The feature instance
     * @param <T> The feature type
     */
    public <T> T feature(Class<T> clazz) {
        return clazz.cast(featureContext.loadedFeatures.get(clazz));
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    public @NotNull C config() {
        return config;
    }

    public @NotNull EventNode<Event> eventNode() {
        return eventNode;
    }
}
