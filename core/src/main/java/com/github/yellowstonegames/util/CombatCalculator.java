package com.github.yellowstonegames.util;

import com.github.yellowstonegames.data.Mob;

public class CombatCalculator {
    // See this Desmos graph to compare the sqrt() option vs. the abs() option:
    // https://www.desmos.com/calculator/kysjp4felk
    // The sqrt() code has a smooth derivative at all points, but abs() does not.
    // I don't know if this matters.
    private final static float EVASION_FACTOR = 500;

    public static float calculateDamage(Mob attacker, Mob defender) {
        float hit = Math.max(0, attacker.getOffense() - defender.getDefense());
        double precision = attacker.getAccuracy() - defender.getEvasion();
        float factoredDefense = (float)(1 + precision / Math.sqrt(EVASION_FACTOR + precision * precision));

        return hit * factoredDefense;
    }
}
