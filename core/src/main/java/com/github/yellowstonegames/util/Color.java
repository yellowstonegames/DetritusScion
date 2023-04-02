package com.github.yellowstonegames.util;

import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.FullPalette;

/**
 * Try to use int colors, either Oklab (using the methods in DescriptiveColor to edit them) or RGBA.
 */
public class Color {
    public static int[][] randomColors(int innerSize, RNG rng) {
    int[][] cs = new int[8][innerSize];
    for (int i = 0; i < 8; i++) {
        for (int j = 0; j < innerSize; j++) {
            cs[i][j] = FullPalette.randomColorWheel(rng);
        }
    }
    return cs;
}

    public static int progressiveLighten(int color) {
        return DescriptiveColor.lighten(color, (1f - DescriptiveColor.lightness(color)) * 0.4f);
    }
}
