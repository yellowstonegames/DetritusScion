package com.github.yellowstonegames.mobs;

import com.github.tommyettinger.ds.ObjectFloatOrderedMap;

public interface HasStats {
    ObjectFloatOrderedMap<String> getBaseStats();
    ObjectFloatOrderedMap<String> getStats();

    /**
     * These can default to 0.
     */
    String[] COMBAT_STATS = new String[]{"offense", "defense", "accuracy", "evasion"};

    float[] COMBAT_VALUES = new float[]{0, 0, 0, 0};
    /**
     * These should default to 1.
     */
    String[] VITAL_STATS = new String[]{"health", "max health", "energy", "max energy"};

    float[] VITAL_VALUES = new float[]{1, 1, 1, 1};
    /**
     * For humanoids, these should default to 1, except for "hand" which should usually be 2.
     * Equipment that uses a slot has a negative value for that slot, typically -1.
     * <br>
     * Note that rings have no slot, since you can potentially just stack rings on the same finger.
     */
    String[] SLOTS = new String[]{"hand", "torso", "waist", "legs", "feet", "neck", "arms", "head", "face"};

    float[] SLOT_VALUES = new float[]{2, 1, 1, 1, 1, 1, 1, 1, 1};

    default HasStats addStats(HasStats other) {
        ObjectFloatOrderedMap<String> modifying = getStats(), modifier = other.getStats();
        modifying.combine(modifier, Float::sum);
        return this;
    }
    default HasStats subtractStats(HasStats other) {
        ObjectFloatOrderedMap<String> modifying = getStats(), modifier = other.getStats();
        modifying.combine(modifier, (main, changer) -> main - changer);
        return this;
    }
}
