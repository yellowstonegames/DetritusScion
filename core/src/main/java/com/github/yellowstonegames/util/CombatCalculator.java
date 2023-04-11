package com.github.yellowstonegames.util;

import com.github.yellowstonegames.mobs.Mob;

public class CombatCalculator {
    private final static double evasionFactor = 20;

    public static double calculateDamage(Mob attacker, Mob defender) {
        double hit = attacker.weapon.damage + attacker.strength - defender.armor.armor;
        hit = Math.max(0, hit);
        double precision = attacker.accuracy - defender.evasion;
        precision = Math.max(0, precision);
        double factoredDefense = precision / (evasionFactor + Math.abs(precision));

        return hit * factoredDefense;
    }
}
