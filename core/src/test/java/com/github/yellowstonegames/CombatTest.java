package com.github.yellowstonegames;

import com.github.yellowstonegames.mobs.Mob;
import com.github.yellowstonegames.util.CombatCalculator;
import org.junit.Test;

public class CombatTest {
    @Test
    public void testCalculator() {
        Mob mobA = new Mob();
        Mob mobB = new Mob();
        System.out.println(CombatCalculator.calculateDamage(mobA, mobB));
    }
}
