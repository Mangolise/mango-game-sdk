package net.mangolise.gamesdk;

import net.minestom.server.tag.Taggable;

import java.util.List;

/**
 * There will only ever be a single instance of this class per jvm runtime.
 * DO NOT run more than one at a time.
 */
public sealed interface Game extends Taggable permits BaseGame {

    /**
     * Sets up the game by first initializing all features.
     */
    void setup();

    /**
     * The game's configuration.
     */
    Record config();

    /**
     * The game's features.
     */
    List<Feature<?>> features();

    interface Feature<G extends Game> {
        void setup(Context<G> context);

        interface Context<G extends Game> {
            G game();

            /**
             * Gets a previously loaded feature. Throws an exception if the feature is not yet loaded.
             */
            Feature<G> feature(Class<? extends Feature<? super G>> feature);

            /**
             * Adds a task that is run on game shutdown.
             */
            void cleanup(Runnable cleanupTask);
        }
    }
}
