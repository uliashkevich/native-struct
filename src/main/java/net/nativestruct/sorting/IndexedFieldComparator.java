package net.nativestruct.sorting;

/**
 * Comparator interface for indexed struct fields.
 */
public interface IndexedFieldComparator {
    /**
     * Compare structs specified by their indexes.
     *
     * @param leftIndex First struct index.
     * @param rightIndex Second struct index.
     * @return If the first struct is less or equal to the second struct.
     */
    boolean lessOrEqual(int leftIndex, int rightIndex);
}
