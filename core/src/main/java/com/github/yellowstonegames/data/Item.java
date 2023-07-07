package com.github.yellowstonegames.data;

import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.tommyettinger.textra.Font;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.FullPalette;
import com.github.yellowstonegames.extensions.com.github.tommyettinger.random.EnhancedRandom.EnhancedRandomExtensions;
import com.github.yellowstonegames.files.Settings;
import com.github.yellowstonegames.glyph.GlyphActor;
import com.github.yellowstonegames.glyph.GlyphGrid;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.util.RNG;
import com.github.yellowstonegames.util.Replicable;
import com.github.yellowstonegames.util.Text;

public class Item implements HasStats, Replicable {
    public final int identifier = ++Settings.ID_COUNTER;
    public static final String ITEM_CHARS = Text.USABLE_SYMBOLS;
    public long glyph;
    public transient GlyphActor actor;
    public String name;

    public ObjectFloatOrderedMap<String> baseStats =
            new ObjectFloatOrderedMap<>(COMBAT_STATS.length + VITAL_STATS.length + 2);
    {
        baseStats.putAll(COMBAT_STATS, COMBAT_VALUES);
        baseStats.putAll(VITAL_STATS, VITAL_VALUES);
    }

    public ObjectFloatOrderedMap<String> stats = new ObjectFloatOrderedMap<>(baseStats);

    @Override
    public ObjectFloatOrderedMap<String> getBaseStats() {
        return baseStats;
    }

    @Override
    public ObjectFloatOrderedMap<String> getStats() {
        return stats;
    }

    public Item() {
        this(Font.applyColor(ITEM_CHARS.charAt(RNG.rng.nextInt(ITEM_CHARS.length())),
                DescriptiveColor.lerpColors(
                        EnhancedRandomExtensions.randomElement(RNG.rng, FullPalette.COLOR_WHEEL_PALETTE_MID),
                        DescriptiveColor.SILVER, 0.4f)),
                null, null, null, null);

    }
    public Item(long representation, GlyphGrid gg, String name, Coord position, ObjectFloatOrderedMap<String> statReplacements) {
        glyph = representation;
        actor = new GlyphActor(glyph, gg == null ? null : gg.getFont());
        if(position != null)
            actor.setLocation(position);
        else
            actor.setVisible(false);
        if(statReplacements != null)
        {
            baseStats.putAll(statReplacements);
            stats.putAll(statReplacements);
        }
        if(name != null)
            this.name = name;
        else {
            this.name = Text.thesaurus.process("weapon`noun` of ancient_egyptian`Gen`");
        }
    }

    public Item makeActor(Font font) {
        if(font != null)
            actor = new GlyphActor(this.glyph, font);
        return this;
    }

    public Item setPosition(Coord position){
        if(position != null)
        {
            actor.setLocation(position);
            actor.setVisible(true);
        }
        else
            actor.setVisible(false);
        return this;
    }

    @Override
    public String toString() {
        return "'" + (char)glyph + "' " + name;
    }

    /**
     * Gets the identifier for this object, as an int.
     *
     * @return the int identifier for this
     */
    @Override
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Creates a replica of this object, which is a copy except that it has its own, different identifying int.
     * This uses Manifold's Self annotation to ensure implementing classes return their own class, not Replicable.
     *
     * @return a copy of this with a different identifier
     */
    @Override
    public Item replicate() {
        Item replica = new Item(glyph, null, null, null, null);
        replica.baseStats.clear();
        replica.baseStats.putAll(baseStats);
        replica.stats.clear();
        replica.stats.putAll(stats);
        replica.makeActor(actor.font).setPosition(actor.getLocation());
        replica.name = name;
        return replica;
    }
}
