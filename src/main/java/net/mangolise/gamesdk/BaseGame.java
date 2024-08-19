package net.mangolise.gamesdk;

import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public non-sealed abstract class BaseGame<C extends Record> implements Game {

    protected TagHandler tagHandler = TagHandler.newHandler();

    protected final C config;
    protected BaseGame(C config) {
        this.config = config;
    }

    protected final List<Runnable> cleanupTasks = new CopyOnWriteArrayList<>();

    @Override
    public void setup() {
        // setup all the features
        FeatureContext context = new FeatureContext();

        for (Feature<?> feature : features()) {
            //noinspection unchecked
            feature.setup(context);
            context.loadedFeatures.put(feature.getClass(), feature);
        }

        cleanupTasks.addAll(context.cleanupTasks);

        // register the cleanup tasks
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Runnable cleanupTask : cleanupTasks) {
                cleanupTask.run();
            }
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

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    public @NotNull C config() {
        return config;
    }
}
