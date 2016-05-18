package net.nativestruct;

/**
 * Abstraction representing a projection of a struct vector.
 *
 * @param <T> Accessor type.
 */
public interface StructProjection<T> {
    /**
     * @return Accessor instance, giving access to vector elements.
     */
    T accessor();

    /**
     * @return Vector size.
     */
    int size();

    /**
     * @return Current record index. By default it equals -1.
     */
    int current();

    /**
     * Updates the current record index for all the field accessors and enclosed struct records.
     *
     * @param index A new record index.
     */
    void current(int index);
}
