package com.github.yellowstonegames.util;

import com.github.yellowstonegames.mobs.Mob;

public class CombatCalculator {
    private final static float EVASION_FACTOR = 20;

    public static float calculateDamage(Mob attacker, Mob defender) {
        float hit = Math.max(0, attacker.offense - defender.defense);
        float precision = attacker.accuracy - defender.evasion;
        float factoredDefense = 1 + precision / (EVASION_FACTOR + Math.abs(precision));

        return hit * factoredDefense;
    }
}
