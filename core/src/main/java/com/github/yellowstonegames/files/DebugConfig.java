package com.github.yellowstonegames.files;

import com.anyicomplex.gdx.svm.CollectForGDXJsonSerialization;

/**
 * Debug configuration, not all need to appear in in-game settings.
 */
@CollectForGDXJsonSerialization
public class DebugConfig {

    public boolean debugActive;
    public boolean odinView;
    public boolean showFPS;
    public DebugConfig() {

    }
}
