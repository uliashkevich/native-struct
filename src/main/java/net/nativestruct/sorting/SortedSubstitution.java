package net.nativestruct.sorting;

/**
 * The abstraction encapsulates struct vector records substitution according to specified order.
 */
public interface SortedSubstitution {
    /**
     * @return Comparator used for comparison of indexed struct fields.
     */
    IndexedFieldComparator comparator();

    /**
     * @return Substitution array, which can be used to reorder struct records.
     */
    OrderingSubstitution ordering();
}
