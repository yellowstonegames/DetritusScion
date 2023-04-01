package com.github.yellowstonegames.util;

public class Color {

    public static com.badlogic.gdx.graphics.Color[][] randomColors(int innerSize, RNG rng) {
    com.badlogic.gdx.graphics.Color[][] cs = new com.badlogic.gdx.graphics.Color[8][innerSize];
    for (int i = 0; i < 8; i++) {
        for (int j = 0; j < innerSize; j++) {
            cs[i][j] = SColor.randomColorWheel(rng);
        }
    }
    return cs;
}

    public static float progressiveLighten(float color) {
        return SColor.toEditedFloat(color, 0f, 0f, (1.0f - SColor.lumaOfFloat(color)) * 0.4f, 1f);
    }
}
