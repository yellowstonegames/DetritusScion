package com.github.yellowstonegames.mobs;

import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.yellowstonegames.glyph.GlyphActor;

public class Mob implements HasStats {
    public long glyph;
    public transient GlyphActor actor;

    public ObjectFloatOrderedMap<String> baseStats = new ObjectFloatOrderedMap<>(COMBAT_STATS, COMBAT_VALUES);

    {
        baseStats.putAll(VITAL_STATS, VITAL_VALUES);
        baseStats.putAll(SLOTS, SLOT_VALUES);
    }

    public ObjectFloatOrderedMap<String> stats = new ObjectFloatOrderedMap<>(baseStats);

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

    public float getEnergy() {
        return stats.get("energy");
    }

    public void setEnergy(float energy) {
        stats.put("energy", Math.min(Math.max(energy, 0), stats.get("max energy")));
    }

    public float getMaxEnergy() {
        return stats.get("max energy");
    }

    public void setMaxEnergy(float maxEnergy) {
        boolean fullyEnergized = (stats.get("max energy") <= energy);
        maxEnergy = Math.max(maxEnergy, 0);
        stats.put("max energy", maxEnergy);
        if(energy > maxEnergy || fullyEnergized) {
            energy = maxEnergy;
        }
    }
}
