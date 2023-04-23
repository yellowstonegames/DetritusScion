package com.github.yellowstonegames.files;

import com.anyicomplex.gdx.svm.CollectForReflection;

/**
 * Debug configuration, not all need to appear in in-game settings.
 */
@CollectForReflection
public class DebugConfig {

    public boolean debugActive;
    public boolean odinView;
    public boolean showFPS;
    public DebugConfig() {

    }
}
