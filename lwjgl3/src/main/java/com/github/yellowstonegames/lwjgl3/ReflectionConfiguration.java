package com.github.yellowstonegames.lwjgl3;

import com.github.yellowstonegames.glyph.GridAction;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import com.github.yellowstonegames.glyph.MoreActions;

public class ReflectionConfiguration implements Feature {
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        RuntimeReflection.register(MoreActions.LenientSequenceAction.class.getDeclaredConstructors());
        RuntimeReflection.register(MoreActions.LenientParallelAction.class.getDeclaredConstructors());
        RuntimeReflection.register(GridAction.CloudAction.class.getDeclaredConstructors());
        RuntimeReflection.register(GridAction.ExplosionAction.class.getDeclaredConstructors());
        RuntimeReflection.register(GridAction.GibberishAction.class.getDeclaredConstructors());
        RuntimeReflection.register(GridAction.PulseAction.class.getDeclaredConstructors());
    }
}
