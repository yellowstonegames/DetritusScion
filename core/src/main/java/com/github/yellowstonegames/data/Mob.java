package com.github.yellowstonegames.data;

import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.textra.Font;
import com.github.yellowstonegames.core.FullPalette;
import com.github.yellowstonegames.glyph.GlyphActor;
import com.github.yellowstonegames.glyph.GlyphGrid;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.util.RNG;
import com.github.yellowstonegames.util.Text;

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
    protected String name;

    public ObjectFloatOrderedMap<String> stats = new ObjectFloatOrderedMap<>(baseStats);

    public ObjectList<Item> equipment = new ObjectList<>(16);

    public transient Runnable onDeath;

    public Mob() {
        glyph = Font.applyColor(Text.USABLE_LETTERS.charAt(RNG.rng.nextInt(Text.USABLE_LETTERS.length())),
                FullPalette.COLORS_BY_HUE.random(RNG.rng));
        actor = new GlyphActor(glyph, null);
        Language lang = RNG.rng.randomElement(Language.romanizedHumanLanguages);
        name = lang.word(RNG.rng, true) + " " + lang.word(RNG.rng, true);
    }

    public Mob(GlyphGrid gg, Coord position) {
        Font font = gg.getFont();

        char c = Text.USABLE_LETTERS.charAt(RNG.rng.nextInt(Text.USABLE_LETTERS.length()));
        int problems = 0;
        while (!font.mapping.containsKey(c) && ++problems < 10)
            c = Text.USABLE_LETTERS.charAt(RNG.rng.nextInt(Text.USABLE_LETTERS.length()));
        if(problems == 10)
            c = (char)RNG.rng.nextInt('A', 'Z'+1);

        glyph = Font.applyColor(c,
                FullPalette.COLORS_BY_HUE.random(RNG.rng));
        actor = new GlyphActor(glyph, font);
        if(position != null)
            actor.setLocation(position);
        else
            actor.setVisible(false);
        this.onDeath = () -> gg.removeActor(actor);
        Language lang = RNG.rng.randomElement(Language.romanizedHumanLanguages);
        name = lang.word(RNG.rng, true) + " " + lang.word(RNG.rng, true);
    }

    public Mob(GlyphGrid gg, Coord position, EnhancedRandom chaos) {
        Font font = gg.getFont();
        char c = Text.USABLE_LETTERS.charAt(chaos.nextInt(Text.USABLE_LETTERS.length()));
        int problems = 0;
        while (!font.mapping.containsKey(c) && ++problems < 10)
            c = Text.USABLE_LETTERS.charAt(chaos.nextInt(Text.USABLE_LETTERS.length()));
        if(problems == 10)
            c = (char)chaos.nextInt('A', 'Z'+1);

        glyph = Font.applyColor(c,
                FullPalette.COLORS_BY_HUE.random(chaos));
        actor = new GlyphActor(glyph, font);
        if(position != null)
            actor.setLocation(position);
        else
            actor.setVisible(false);
        this.onDeath = () -> gg.removeActor(actor);
        Language lang = chaos.randomElement(Language.romanizedHumanLanguages);
        name = lang.word(chaos, true) + " " + lang.word(chaos, true);
    }

    public Mob(GlyphGrid gg, Coord position, long glyph) {
        Font font = gg.getFont();
        this.glyph = glyph;
        actor = new GlyphActor(this.glyph, font);
        if(position != null)
        {
            actor.setLocation(position);
            glyph ^= position.hashCode();
        }
        else
            actor.setVisible(false);
        this.onDeath = () -> gg.removeActor(actor);
        Language lang = Language.romanizedHumanLanguages[Hasher.randomize3Bounded(glyph + 123, Language.romanizedHumanLanguages.length)];
        name = lang.word(Hasher.randomize3(glyph + 456), true) + " " + lang.word(Hasher.randomize3(glyph + 789), true);
    }

    public Mob(GlyphGrid gg, Coord position, long glyph, Runnable onDeath, ObjectFloatOrderedMap<String> statReplacements) {
        Font font = gg.getFont();
        this.glyph = glyph;
        actor = new GlyphActor(this.glyph, font);
        if(position != null)
        {
            actor.setLocation(position);
            glyph ^= position.hashCode();
        }
        else
            actor.setVisible(false);
        if(onDeath != null)
            this.onDeath = onDeath;
        else
            this.onDeath = () -> gg.removeActor(actor);
        if(statReplacements != null)
        {
            baseStats.putAll(statReplacements);
            stats.putAll(statReplacements);
        }
        Language lang = Language.romanizedHumanLanguages[Hasher.randomize3Bounded(glyph + 123, Language.romanizedHumanLanguages.length)];
        name = lang.word(Hasher.randomize3(glyph + 456), true) + " " + lang.word(Hasher.randomize3(glyph + 789), true);
    }

    public Mob(GlyphGrid gg, Coord position, long glyph, Runnable onDeath, ObjectFloatOrderedMap<String> statReplacements,
               String name) {
        Font font = gg.getFont();
        this.glyph = glyph;
        actor = new GlyphActor(this.glyph, font);
        if(position != null)
        {
            actor.setLocation(position);
            glyph ^= position.hashCode();
        }
        else
            actor.setVisible(false);
        if(onDeath != null)
            this.onDeath = onDeath;
        else
            this.onDeath = () -> gg.removeActor(actor);
        if(statReplacements != null)
        {
            baseStats.putAll(statReplacements);
            stats.putAll(statReplacements);
        }
        if(name != null)
            this.name = name;
        else {
            Language lang = Language.romanizedHumanLanguages[Hasher.randomize3Bounded(glyph + 123, Language.romanizedHumanLanguages.length)];
            name = lang.word(Hasher.randomize3(glyph + 456), true) + " " + lang.word(Hasher.randomize3(glyph + 789), true);
        }
    }

    @Override
    public ObjectFloatOrderedMap<String> getBaseStats() {
        return baseStats;
    }

    @Override
    public ObjectFloatOrderedMap<String> getStats() {
        return stats;
    }

    /**
     * Equips an item, adding its stats to the Mob's if the Mob has adequate slots.
     * @param item a non-null Item; if successfully donned, will be made invisible
     * @return this, for chaining
     */
    public Mob don(Item item) {
        if(equipment.contains(item))
            return this; // if it's already equipped, do nothing.
        for(String slot : HasStats.SLOTS){
            if(stats.getOrDefault(slot, 0f) < item.baseStats.getOrDefault(slot, -Float.MAX_VALUE)) {
                return this; // cannot equip because there are inadequate slots.
            }
        }
        if(item.actor != null)
            item.actor.setVisible(false);
        addStats(item);
        equipment.add(item);
        return this;
    }

    /**
     * Removes an item from a Mob's equipment, restoring slots and placing the Item in
     * the Mob's cell (made visible).
     * @param item a non-null Item in the Mob's equipment
     * @return this, for chaining.
     */
    public Mob doff(Item item) {
        if(equipment.remove(item)) {
            // if the Item was actually removed from equipment, then put it on the floor.
            subtractStats(item);
            if (item.actor != null && this.actor != null) {
                item.actor.setLocation(this.actor.getLocation());
                item.actor.setVisible(true);
            }
        }
        return this;
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
        float health = stats.get("health");
        boolean fullyHealed = (stats.get("max health") <= health);
        maxHealth = Math.max(maxHealth, 0);
        stats.put("max health", maxHealth);
        if(health > maxHealth || fullyHealed) {
            setHealth(maxHealth);
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
        float energy = stats.get("energy");
        boolean fullyEnergized = (stats.get("max energy") <= energy);
        maxEnergy = Math.max(maxEnergy, 0);
        stats.put("max energy", maxEnergy);
        if(energy > maxEnergy || fullyEnergized) {
            setEnergy(maxEnergy);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if(actor != null)
            actor.setName(name);
    }

    @Override
    public String toString() {
        return (char)glyph + " " + name;
    }
}
