/**
 * MIT License
 *
 * Copyright (c) 2016 by Vlad Liashkevich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.nativestruct.sorting;

import java.util.Comparator;

/**
 * The class performs sorting of record indexes according to ordering, specified by a struct field.
 * It returns index substitution array, which is used to reorder struct records.
 */
public abstract class AbstractSortedSubstitution {
    private static final int INSERTION_SORT_THRESHOLD = 1;

    private final IndexedFieldComparator comparator;
    private final int[] indexes;
    private final int[] shadow;

    /**
     * Construct instance.
     *
     * @param size Vector size.
     * @param comparator Comparator for record comparison, specified by their indexes.
     */
    public AbstractSortedSubstitution(int size, IndexedFieldComparator comparator) {
        this.comparator = comparator;
        this.indexes = new int[size];
        this.shadow = new int[size];
    }

    /**
     * Performs sorting of indexes.
     *
     * @return Substitution array, which can be used to reorder struct records.
     */
    public final int[] substitution() {
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }

        sort(0, indexes.length);

        for (int i = 0; i < indexes.length; i++) {
            shadow[indexes[i]] = i;
        }

        return shadow;
    }

    /**
     * Recursive sort algorithm. It uses insertion sort for small sub-arrays and merge sort for
     * bigger ones.
     *
     * @param lower Lower bound of sub-array to sort.
     * @param upper Lower bound of sub-array to sort.
     */
    private void sort(int lower, int upper) {
        int count = upper - lower;
        if (count <= INSERTION_SORT_THRESHOLD) {
            insertionSort(lower, upper);
        } else {
            int middle = (lower + upper) / 2;
            sort(lower, middle);
            sort(middle, upper);
            merge(lower, upper, middle);
        }
    }

    /**
     * Insertion sort algorithm for small sub-array.
     *
     * @param lower Lower bound of sub-array to sort.
     * @param upper Lower bound of sub-array to sort.
     */
    private void insertionSort(int lower, int upper) {
        for (int i = lower + 1; i < upper; i++) {
            int prev = indexes[i - 1];
            int current = indexes[i];

            if (!comparator.lessOrEqual(prev, current)) {
                indexes[i] = prev;
                int insertion = i - 1;
                while (--insertion >= lower
                        && !comparator.lessOrEqual(indexes[insertion], current)) {
                    indexes[insertion + 1] = indexes[insertion];
                }
                indexes[insertion + 1] = current;
            }
        }
    }

    /**
     * Merge sorted lower and upper sub-arrays.
     *
     * @param lower Lower bound of sub-array to sort.
     * @param upper Upper bound of sub-array to sort.
     * @param middle Middle element.
     */
    private void merge(int lower, int upper, int middle) {
        skipSmallestAndMerge(lower, upper, middle);
    }

    private void skipSmallestAndMerge(int originalLower, int upper, int middle) {
        int lower = originalLower;
        int leastUpper = indexes[middle];
        while (comparator.lessOrEqual(indexes[lower], leastUpper)) {
            if (++lower >= middle) {
                return;
            }
        }

        skipLargestAndMerge(lower, upper, middle);
    }

    private void skipLargestAndMerge(int lower, int originalUpper, int middle) {
        int upper = originalUpper;
        int mostLower = indexes[middle - 1];
        while (comparator.lessOrEqual(mostLower, indexes[upper - 1])) {
            if (--upper <= middle) {
                return;
            }
        }

        mergeNoSmallestLargest(lower, upper, middle);
    }

    private void mergeNoSmallestLargest(int lower, int upper, int middle) {
        int first = lower;
        int second = middle;
        int target = lower;

        shadow[target++] = indexes[second++];

        while (second < upper) {
            int left = indexes[first];
            int right = indexes[second];
            if (comparator.lessOrEqual(left, right)) {
                shadow[target++] = left;
                first++;
            } else {
                shadow[target++] = right;
                second++;
            }
        }

        System.arraycopy(indexes, first, indexes, target, middle - first);
        System.arraycopy(shadow, lower, indexes, lower, upper - lower - (middle - first));
    }

    /**
     * Comparator interface for indexed struct fields.
     */
    interface IndexedFieldComparator {
        /**
         * Compare structs specified by their indexes.
         *
         * @param leftIndex First struct index.
         * @param rightIndex Second struct index.
         * @return If the first struct is less or equal to the second struct.
         */
        boolean lessOrEqual(int leftIndex, int rightIndex);
    }

    /**
     * Sorted substitution implement for integer fields.
     */
    public static class Integers extends AbstractSortedSubstitution {
        /**
         * Construct instance.
         *
         * @param integers Integer struct fields array.
         * @param size Struct vector size.
         * @param width The number of integer fields in the struct.
         * @param field Index of the field to be used for sorting.
         */
        public Integers(int[] integers, int size, int width, int field) {
            super(size, (left, right)
                -> integers[width * left + field] <= integers[width * right + field]);
        }
    }

    /**
     * Sorted substitution implement for double fields.
     */
    public static class Doubles extends AbstractSortedSubstitution {
        /**
         * Construct instance.
         *
         * @param doubles Double struct fields array.
         * @param size Struct vector size.
         * @param width The number of double fields in the struct.
         * @param field Index of the field to be used for sorting.
         */
        public Doubles(double[] doubles, int size, int width, int field) {
            super(size, (left, right)
                -> doubles[width * left + field] <= doubles[width * right + field]);
        }
    }

    /**
     * Sorted substitution implement for object fields.
     */
    public static class Objects extends AbstractSortedSubstitution {
        /**
         * Construct instance.
         *
         * @param objects Object struct fields array.
         * @param size Struct vector size.
         * @param width The number of integer fields in the struct.
         * @param field Index of the field to be used for sorting.
         * @param objectComparator Comparator used for field value comparison.
         * @param <U> Comparator generic type.
         */
        @SuppressWarnings("unchecked")
        public <U> Objects(Object[] objects, int size, int width, int field,
                          Comparator<U> objectComparator) {
            super(size, (left, right) -> objectComparator.compare(
                    (U) objects[width * left + field],
                    (U) objects[width * right + field]) <= 0);
        }
    }
}
