package com.github.yellowstonegames.mobs;

import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.yellowstonegames.glyph.GlyphActor;

public class Mob implements HasStats {
    public long glyph;
    public transient GlyphActor actor;

    public ObjectFloatOrderedMap<String> baseStats = ObjectFloatOrderedMap.with(
            "offense", 0, "defense", 0, "accuracy", 0, "evasion", 0, "health", 1, "max health", 1);

    public ObjectFloatOrderedMap<String> stats = ObjectFloatOrderedMap.with(
            "offense", 0, "defense", 0, "accuracy", 0, "evasion", 0, "health", 1, "max health", 1);

    public transient Runnable onDeath;

    @Override
    public ObjectFloatOrderedMap<String> getBaseStats() {
        return baseStats;
    }

    @Override
    public ObjectFloatOrderedMap<String> getStats() {
        return stats;
    }

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

    public float getHealth() {
        return stats.get("health");
    }

    public void setHealth(float health) {
        stats.put("health", Math.min(Math.max(health, 0), stats.get("max health")));
        if(health <= 0 && onDeath != null)
            onDeath.run();
    }

    public float getMaxHealth() {
        return stats.get("max health");
    }

    public void setMaxHealth(float maxHealth) {
        boolean fullyHealed = (stats.get("max health") <= health);
        maxHealth = Math.max(maxHealth, 0);
        stats.put("max health", maxHealth);
        if(health > maxHealth || fullyHealed) {
            health = maxHealth;
        }
    }
}
