package com.github.yellowstonegames.mobs;

import com.github.yellowstonegames.glyph.GlyphActor;

public class Mob {
    public long glyph;
    public transient GlyphActor actor;

    public double health;
    public double strength;
    public double accuracy;
    public double evasion;

    public Weapon weapon;
    public Armor armor;
}
