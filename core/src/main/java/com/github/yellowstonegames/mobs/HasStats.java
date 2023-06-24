package com.github.yellowstonegames.mobs;

import com.github.tommyettinger.ds.ObjectFloatOrderedMap;

public interface HasStats {
    ObjectFloatOrderedMap<String> getBaseStats();
    ObjectFloatOrderedMap<String> getStats();
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
