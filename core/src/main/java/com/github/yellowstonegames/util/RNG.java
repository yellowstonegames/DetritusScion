package com.github.yellowstonegames.util;

import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.Deserializer;

import java.util.List;

public class RNG extends AceRandom {

    /**
     * A publicly available rng to allow for use without constructing.
     * This is meant for cases where the seed does not matter.
     */
    public static final RNG rng = new RNG();

    /**
     * Creates a new RNG with a random state.
     */
    public RNG() {
        super();
    }

    /**
     * Creates a new RNG with the given seed; all {@code long} values are permitted.
     * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
     *
     * @param seed any {@code long} value
     */
    public RNG(long seed) {
        super(seed);
    }

    /**
     * Creates a new RNG with the given five states; all {@code long} values are permitted.
     * These states will be used verbatim.
     *
     * @param stateA any {@code long} value
     * @param stateB any {@code long} value
     * @param stateC any {@code long} value
     * @param stateD any {@code long} value
     * @param stateE any {@code long} value
     */
    public RNG(long stateA, long stateB, long stateC, long stateD, long stateE) {
        super(stateA, stateB, stateC, stateD, stateE);
    }

    /**
     * This initializes all 5 states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here.
     *
     * @param seed the initial seed; may be any long
     */
    @Override
    public void setSeed (long seed) {
        // Based on (not identical to) the Speck block cipher's key expansion.
        // Only uses add, bitwise rotation, and XOR operations.
        long s0 = seed, s1 = seed ^ 0xC6BC279692B5C323L, ctr = seed ^ 0x1C69B3F74AC4AE35L;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr += 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateA = s0;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr += 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateB = s0;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr += 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateC = s0;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr += 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateD = s0;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr += 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateE = s0;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr += 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateA += s0;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr += 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateB += s0;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr += 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateC += s0;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr += 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateD += s0;
        s1 = (s1 << 56 | s1 >>>  8) + s0 ^ (ctr + 0xBEA225F9EB34556DL);
        s0 = (s0 <<  3 | s0 >>> 61) ^ s1;
        stateE += s0;
    }

    @Override
    public RNG copy() {
        return new RNG(stateA, stateB, stateC, stateD, stateE);
    }

    /**
     * Returns a value from a uniform distribution from min (inclusive) to max
     * (exclusive). If min is greater than max, min is still inclusive, and max
     * is still exclusive.
     * <br>
     * This is an alias for {@link #nextDouble(double, double)}.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    public double between(double min, double max) {
        return nextDouble(min, max);
    }

    /**
     * Returns a value from a uniform distribution from min (inclusive) to max
     * (exclusive). If min is greater than max, min is still inclusive, and max
     * is still exclusive.
     * <br>
     * This is an alias for {@link #nextDouble(double, double)}.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    public float between(float min, float max) {
        return nextFloat(min, max);
    }

    /**
     * Returns a value between min (inclusive) and max (exclusive).
     * <p>
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     * <br>
     * This is an alias for {@link #nextInt(int, int)}.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    public int between(int min, int max) {
        return nextInt(min, max);
    }

    /**
     * Returns a random element from the provided array and maintains object
     * type.
     * <br>
     * This wraps {@link #randomElement(Object[])} in a check for null or
     * empty arrays, in which case this returns null.
     *
     * @param <T>   the type of the returned object
     * @param array the array to get an element from
     * @return the randomly selected element
     */
    public <T> T getRandomElement(T[] array) {
        if (array == null || array.length < 1) {
            return null;
        }
        return randomElement(array);
    }

    /**
     * Returns a random element from the provided list. If the list is empty
     * then null is returned.
     * <br>
     * This wraps {@link #randomElement(List)} in a check for null or empty
     * lists, in which case this returns null.
     *
     * @param <T>  the type of the returned object
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    public <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return randomElement(list);
    }

    public boolean randomElement(boolean[] arr){
        if (arr == null || arr.length == 0) return false;
        return arr[nextInt(arr.length)];
    }

    public byte randomElement(byte[] arr){
        if (arr == null || arr.length == 0) return 0;
        return arr[nextInt(arr.length)];
    }

    public short randomElement(short[] arr){
        if (arr == null || arr.length == 0) return 0;
        return arr[nextInt(arr.length)];
    }

    public int randomElement(int[] arr){
        if (arr == null || arr.length == 0) return 0;
        return arr[nextInt(arr.length)];
    }

    public long randomElement(long[] arr){
        if (arr == null || arr.length == 0) return 0;
        return arr[nextInt(arr.length)];
    }

    public float randomElement(float[] arr){
        if (arr == null || arr.length == 0) return 0;
        return arr[nextInt(arr.length)];
    }

    public double randomElement(double[] arr){
        if (arr == null || arr.length == 0) return 0;
        return arr[nextInt(arr.length)];
    }

    public char randomElement(char[] arr){
        if (arr == null || arr.length == 0) return '\u0000';
        return arr[nextInt(arr.length)];
    }

    public char randomElement(CharSequence str){
        if (str == null || str.isEmpty()) return '\u0000';
        return str.charAt(nextInt(str.length()));
    }

    // Serialization code

    @Override
    public String getTag() {
        return "RNGR"; // four characters for compatibility.
    }

    static {
        Deserializer.register(new RNG());
    }
}
