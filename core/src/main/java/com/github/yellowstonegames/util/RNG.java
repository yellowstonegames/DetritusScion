package com.github.yellowstonegames.util;

import com.github.tommyettinger.random.Deserializer;
import com.github.tommyettinger.random.WhiskerRandom;

import java.util.List;

public class RNG extends WhiskerRandom {

    /**
     * A publicly available rng to allow for use without constructing.
     * This is meant for cases where the seed does not matter.
     */
    public static final RNG rng = new RNG();

    /**
     * Creates a new WhiskerRandom with a random state.
     */
    public RNG() {
        super();
    }

    /**
     * Creates a new WhiskerRandom with the given seed; all {@code long} values are permitted.
     * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
     *
     * @param seed any {@code long} value
     */
    public RNG(long seed) {
        super(seed);
    }

    /**
     * Creates a new WhiskerRandom with the given four states; all {@code long} values are permitted.
     * These states will be used verbatim.
     *
     * @param stateA any {@code long} value
     * @param stateB any {@code long} value
     * @param stateC any {@code long} value
     * @param stateD any {@code long} value
     */
    public RNG(long stateA, long stateB, long stateC, long stateD) {
        super(stateA, stateB, stateC, stateD);
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

    // Serialization code

    @Override
    public String getTag() {
        return "RNGR"; // four characters for compatibility.
    }

    static {
        Deserializer.register(new RNG());
    }
}
