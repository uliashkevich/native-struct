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

/**
 * Performs reduce operation over a vector of structs. It iterates over source vector, searches
 * the key in the target vector and then accumulates the result.
 *
 * @param <T> Struct accessor type.
 */
public final class InsertionReducer<T> implements Reducer<T> {
    private final StructVector<T> source;
    private final Field field;
    private StructVector<T> target;

    /**
     * Constructs Reduce instance grouping records by the given field.
     *
     * @param source Struct vector.
     * @param field Field by which grouping is performed.
     */
    public InsertionReducer(StructVector<T> source, Field field) {
        this.source = source;
        this.field = field;
    }

    /**
     * Constructs Reduce instance grouping records by the given field.
     *
     * @param source Struct vector.
     * @param field Field by which grouping is performed.
     */
    public InsertionReducer(StructVector<T> source, String field) {
        this(source, source.field(field));
    }

    @Override
    public Reducer<T> into(StructVector<T> vector) {
        this.target = vector;
        return this;
    }

    @Override
    public StructVector<T> with(BiConsumer<T, T> consumer) {
        target.resize(0);

        T sourceAccessor = source.accessor();
        T targetAccessor = target.accessor();

        int size = source.size();
        for (int sourceIndex = 0; sourceIndex < size; sourceIndex++) {
            int targetIndex;
            if (field.isType(int.class)) {
                targetIndex = target.binarySearch(
                        field, source.fieldValueInteger(field, sourceIndex));
            } else if (field.isType(double.class)) {
                targetIndex = target.binarySearch(
                        field, source.fieldValueDouble(field, sourceIndex));
            } else {
                targetIndex = target.binarySearch(
                        field, (Comparable) source.fieldValueObject(field, sourceIndex));
            }

            if (targetIndex < 0) {
                targetIndex = -1 - targetIndex;
                target.insert(targetIndex, 1);
                target.updateFrom(targetIndex, source, sourceIndex);
            } else {
                source.current(sourceIndex);
                target.current(targetIndex);
                consumer.accept(targetAccessor, sourceAccessor);
            }
        }

        return target;
    }
}
