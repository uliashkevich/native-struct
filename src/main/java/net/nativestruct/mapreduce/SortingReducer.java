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
package net.nativestruct.mapreduce;

import java.util.function.BiConsumer;

import net.nativestruct.StructVector;
import net.nativestruct.implementation.field.Field;
import net.nativestruct.sorting.IndexedFieldComparator;
import net.nativestruct.sorting.SortedProjection;

/**
 * Performs reduce operation over a vector of structs. It does by sorting vector records and then
 * accumulating result linearly.
 *
 * @param <T> Struct accessor type.
 */
public final class SortingReducer<T> implements Reducer<T> {
    private final StructVector<T> source;
    private final SortedProjection<T> projection;
    private StructVector<T> target;

    /**
     * Constructs Reduce instance grouping records by the given field.
     *
     * @param vector Struct vector.
     * @param field Field by which grouping is performed.
     */
    public SortingReducer(StructVector<T> vector, Field field) {
        this.source = vector;
        this.projection = new SortedProjection<>(source, field);
    }

    /**
     * Constructs Reduce instance grouping records by the given field.
     *
     * @param vector Struct vector.
     * @param field Field by which grouping is performed.
     */
    public SortingReducer(StructVector<T> vector, String field) {
        this(vector, vector.field(field));
    }

    @Override
    public SortingReducer<T> into(StructVector<T> vector) {
        this.target = vector;
        return this;
    }

    @Override
    public StructVector<T> with(BiConsumer<T, T> consumer) {
        target.resize(0);
        T sourceAccessor = projection.accessor();
        T targetAccessor = target.accessor();

        IndexedFieldComparator comparator = projection.substitution().comparator();

        int size = source.size();
        int index = 0;
        while (index < size) {
            int accIndex = projection.sourceIndex(index);

            source.current(accIndex);
            target.insertLast();

            target.updateFrom(target.size() - 1, source, accIndex);

            while (++index < size) {
                int sourceIndex = projection.sourceIndex(index);
                if (!comparator.lessOrEqual(sourceIndex, accIndex)) {
                    break;
                }

                source.current(sourceIndex);
                consumer.accept(targetAccessor, sourceAccessor);
            }
        }

        return target;
    }
}
