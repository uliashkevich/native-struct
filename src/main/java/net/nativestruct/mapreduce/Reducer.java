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

/**
 * Struct vector reduce operation implementation.
 *
 * @param <T> Vector accessor type.
 */
public interface Reducer<T> {
    /**
     * Assigns target vector before performing reduce operation.
     *
     * @param vector The instance of struct vector for storing reduce result.
     * @return This instance.
     */
    Reducer<T> into(StructVector<T> vector);

    /**
     * Performs reduce by applying the specified reduction consumer.
     *
     * @param consumer The first parameter is accumulator,
     *                 the second parameter is value being reduced.
     * @return Struct vector with the reduce result.
     */
    StructVector<T> with(BiConsumer<T, T> consumer);
}
