package net.mangolise.gamesdk;

import net.minestom.server.tag.Taggable;

import java.util.List;

public interface Game extends Taggable {

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
        }
    }
}
