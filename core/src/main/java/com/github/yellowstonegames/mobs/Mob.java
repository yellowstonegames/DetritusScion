package com.github.yellowstonegames.mobs;

import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.textra.Font;
import com.github.yellowstonegames.core.FullPalette;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.glyph.GlyphActor;
import com.github.yellowstonegames.glyph.GlyphGrid;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.util.RNG;

public class Mob implements HasStats {
    public long glyph;
    public transient GlyphActor actor;

    public ObjectFloatOrderedMap<String> baseStats =
            new ObjectFloatOrderedMap<>(COMBAT_STATS.length + VITAL_STATS.length + SLOTS.length);
    {
        baseStats.putAll(COMBAT_STATS, COMBAT_VALUES);
        baseStats.putAll(VITAL_STATS, VITAL_VALUES);
        baseStats.putAll(SLOTS, SLOT_VALUES);
    }

    public ObjectFloatOrderedMap<String> stats = new ObjectFloatOrderedMap<>(baseStats);

    public transient Runnable onDeath;

    public Mob() {
        glyph = Font.applyColor(StringTools.LETTERS.charAt(RNG.rng.nextInt(StringTools.LETTERS.length())),
                FullPalette.COLORS_BY_HUE.random(RNG.rng));
        actor = new GlyphActor(glyph, null);
    }

    public Mob(GlyphGrid gg, Coord position) {
        Font font = gg.getFont();

        char c = StringTools.LETTERS.charAt(RNG.rng.nextInt(StringTools.LETTERS.length()));
        int problems = 0;
        while (!font.mapping.containsKey(c) && ++problems < 10)
            c = StringTools.LETTERS.charAt(RNG.rng.nextInt(StringTools.LETTERS.length()));
        if(problems == 10)
            c = (char)RNG.rng.nextInt('A', 'Z'+1);

        glyph = Font.applyColor(c,
                FullPalette.COLORS_BY_HUE.random(RNG.rng));
        actor = new GlyphActor(glyph, font);
        actor.setLocation(position);
    }

    public Mob(GlyphGrid gg, Coord position, EnhancedRandom chaos) {
        Font font = gg.getFont();
        char c = StringTools.LETTERS.charAt(chaos.nextInt(StringTools.LETTERS.length()));
        int problems = 0;
        while (!font.mapping.containsKey(c) && ++problems < 10)
            c = StringTools.LETTERS.charAt(chaos.nextInt(StringTools.LETTERS.length()));
        if(problems == 10)
            c = (char)chaos.nextInt('A', 'Z'+1);

        glyph = Font.applyColor(c,
                FullPalette.COLORS_BY_HUE.random(chaos));
        actor = new GlyphActor(glyph, font);
        actor.setLocation(position);
    }

    public Mob(GlyphGrid gg, Coord position, long glyph) {
        Font font = gg.getFont();
        this.glyph = glyph;
        actor = new GlyphActor(this.glyph, font);
        actor.setLocation(position);
    }

    public Mob(GlyphGrid gg, Coord position, long glyph, Runnable onDeath, ObjectFloatOrderedMap<String> statReplacements) {
        Font font = gg.getFont();
        this.glyph = glyph;
        actor = new GlyphActor(this.glyph, font);
        actor.setLocation(position);
        if(onDeath != null)
            this.onDeath = onDeath;
        else
            this.onDeath = () -> gg.removeActor(actor);
        if(statReplacements != null)
            baseStats.putAll(statReplacements);
    }

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
