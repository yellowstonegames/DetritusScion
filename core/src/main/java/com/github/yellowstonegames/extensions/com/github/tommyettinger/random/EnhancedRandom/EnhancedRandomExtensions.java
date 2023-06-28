package com.github.yellowstonegames.extensions.com.github.tommyettinger.random.EnhancedRandom;

import com.github.tommyettinger.random.EnhancedRandom;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

@Extension
public class EnhancedRandomExtensions {
    public static boolean randomElement(@This EnhancedRandom thiz, boolean[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static byte randomElement(@This EnhancedRandom thiz, byte[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static short randomElement(@This EnhancedRandom thiz, short[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static int randomElement(@This EnhancedRandom thiz, int[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static long randomElement(@This EnhancedRandom thiz, long[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static float randomElement(@This EnhancedRandom thiz, float[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static double randomElement(@This EnhancedRandom thiz, double[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static char randomElement(@This EnhancedRandom thiz, char[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static char randomElement(@This EnhancedRandom thiz, String str){
        return str.charAt(thiz.nextInt(str.length()));
    }
}
