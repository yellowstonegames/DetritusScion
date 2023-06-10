package com.github.yellowstonegames.util;

import com.github.yellowstonegames.mobs.Mob;

public class CombatCalculator {
    private final static double EVASION_FACTOR = 20;

    public static double calculateDamage(Mob attacker, Mob defender) {
        double hit = Math.max(0, attacker.weapon.damage + attacker.strength - defender.armor.armor);
        double precision = attacker.accuracy - defender.evasion;
        double factoredDefense = 1.0 + precision / (EVASION_FACTOR + Math.abs(precision));

        return hit * factoredDefense;
    }
}
