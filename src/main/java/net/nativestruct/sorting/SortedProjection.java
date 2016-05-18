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

import net.nativestruct.StructProjection;
import net.nativestruct.StructVector;
import net.nativestruct.implementation.field.Field;

/**
 * The projection is based on struct vector records reordering based on a specified sorting.
 * @param <T>
 */
public final class SortedProjection<T> implements StructProjection<T> {
    private final StructVector<T> vector;
    private final SortedSubstitution substitution;
    private final OrderingSubstitution ordering;

    private int current;

    /**
     * Creates projection with sort order by the specified field.
     *
     * @param vector Struct vector.
     * @param field Field that specifies order of the records.
     */
    public SortedProjection(StructVector<T> vector, Field field) {
        this.vector = vector;
        this.substitution = vector.sortedSubstitution(field);
        this.ordering = substitution.ordering();
    }

    /**
     * @return Substitution object.
     */
    public SortedSubstitution substitution() {
        return substitution;
    }

    /**
     * Transforms projection record index into the original index.
     *
     * @param index Record index for the current projection.
     * @return Record index for the underlying struct vector.
     */
    public int sourceIndex(int index) {
        return ordering.forIndex(index);
    }

    @Override
    public T accessor() {
        return vector.accessor();
    }

    @Override
    public int size() {
        return vector.size();
    }

    @Override
    public int current() {
        return current;
    }

    @Override
    public void current(int index) {
        this.current = index;
        vector.current(sourceIndex(index));
    }
}
