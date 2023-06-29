package com.github.yellowstonegames.util;

import com.github.yellowstonegames.core.IIdentified;
import manifold.ext.rt.api.Self;

public interface Replicable extends IIdentified {
    /**
     * Creates a replica of this object, which is a copy except that it has its own, different identifying int.
     * This uses Manifold's Self annotation to ensure implementing classes return their own class, not Replicable.
     * @return a copy of this with a different identifier
     */
    @Self Replicable replicate();
}
