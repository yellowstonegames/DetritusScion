package com.github.yellowstonegames.extensions.com.github.tommyettinger.random.EnhancedRandom;

import com.github.tommyettinger.random.EnhancedRandom;

public class EnhancedRandomExtensions {
    public static boolean randomElement(EnhancedRandom thiz, boolean[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static byte randomElement(EnhancedRandom thiz, byte[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static short randomElement(EnhancedRandom thiz, short[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static int randomElement(EnhancedRandom thiz, int[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static long randomElement(EnhancedRandom thiz, long[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static float randomElement(EnhancedRandom thiz, float[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static double randomElement(EnhancedRandom thiz, double[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static char randomElement(EnhancedRandom thiz, char[] arr){
        return arr[thiz.nextInt(arr.length)];
    }

    public static char randomElement(EnhancedRandom thiz, String str){
        return str.charAt(thiz.nextInt(str.length()));
    }
}
