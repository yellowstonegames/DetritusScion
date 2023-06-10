package com.github.yellowstonegames.mobs;

import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.yellowstonegames.glyph.GlyphActor;

public class Mob {
    public long glyph;
    public transient GlyphActor actor;

    public float health;

    public ObjectFloatOrderedMap<String> stats = ObjectFloatOrderedMap.with(
            "offense", 0, "defense", 0, "accuracy", 0, "evasion", 0, "max health", 1);

    public float getOffense() {
        return stats.get("offense");
    }

    public void setOffense(float offense) {
        stats.put("offense", offense);
    }

    public float getDefense() {
        return stats.get("defense");
    }

    public void setDefense(float defense) {
        stats.put("defense", defense);
    }

    public float getAccuracy() {
        return stats.get("accuracy");
    }

    public void setAccuracy(float accuracy) {
        stats.put("accuracy", accuracy);
    }

    public float getEvasion() {
        return stats.get("evasion");
    }

    public void setEvasion(float evasion) {
        stats.put("evasion", evasion);
    }
}
