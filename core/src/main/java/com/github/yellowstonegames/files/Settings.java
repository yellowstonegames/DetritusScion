package com.github.yellowstonegames.files;

import com.anyicomplex.gdx.svm.CollectForGDXJsonSerialization;
import com.github.tommyettinger.digital.Hasher;

/**
 * User accessible settings, should be displayed in-game.
 */
@CollectForGDXJsonSerialization
public class Settings {

    public Settings() {
    }

    public final String[] advice = new String[]{
        "If you ever want to reset your settings, you can simply delete this file.",
        "If the seed can be read as a long, it will be. Otherwise it will be read as a string."
    };

    // public GameMode mode = GameMode.CRAWL; // TODO - figure out way to set up game mode choice after refactoring

    public String seed = "bananas";

    public int worldWidth = 160;
    public int worldHeight = 160;
    public int worldDepth = 10;

    // In-flight values that shouldn't be saved to settings file
    transient public long seedValue;

    public void calcSeed() {
        try {
            seedValue = Long.parseLong(seed);
        } catch (NumberFormatException ex) {
            seedValue = Hasher.hydrogen.hash64(seed);
        }
    }

}
