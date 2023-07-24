package com.github.yellowstonegames;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import com.badlogic.gdx.utils.reflect.ClassReflection;

public class ArrayExTestMain {
    public static void main(String[] args)
    {
        ArrayEx<SuperClass> a = new ArrayEx<>();

        a.add(new SuperClass());
        a.add(new SuperClass());
        a.add(new SubClass("A"));
        a.add(new SuperClass());
        a.add(new SuperClass());
        a.add(new SubClass("B"));
        a.add(new SuperClass());
        a.add(new SuperClass());
        a.add(new SuperClass());

        System.out.print("Before: ");
        System.out.println(a.toString(", "));
        System.out.print("After: ");
        System.out.println(a.ofType(SubClass.class).toString(", "));
        System.out.print("Or: ");
        Iterable<SuperClass> pit = a.select(new Predicate<SuperClass>() {
            public boolean evaluate(SuperClass item) {
                return ClassReflection.isInstance(SubClass.class, item);
            }
        });
        Array<SubClass> subs = new Array<>(a.ordered, a.size, SubClass.class);
        for(SuperClass sup : pit)
            subs.add((SubClass) sup);

        System.out.println(subs.toString(", "));
    }

    public static class ArrayEx<T> extends Array<T> {
        /** Loops through this Array and returns Array of objects of specified type {@code typeIn} */
        @SuppressWarnings("unchecked")
        public <T1> Array<T1> ofType(Class<T1> typeIn)
        {
            // preserve the ordered status, use size as an upper bound on capacity, use T1 for items
            Array<T1> output = new Array<>(ordered, size, typeIn);
            // "fori" loop tends to be preferred within libGDX, when possible; it permits nesting
            for (int i = 0, n = size; i < n; i++)
            {
                Object o = items[i];
                // ClassReflection is needed for GWT support; it also can't call .cast()
                if (ClassReflection.isInstance(typeIn, o))
                    output.add((T1)o);
            }
            return output;
        }
    }

    private static class SuperClass {
        static int instanceCount = 0;
        protected String name;
        public SuperClass() { name = this.getClass().getSimpleName() + instanceCount++; }

        @Override
        public String toString() { return name; }
    }

    private static class SubClass extends SuperClass {
        public SubClass(String s) { name = s; }
    }
}