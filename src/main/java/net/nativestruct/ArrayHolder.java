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
package net.nativestruct;

/**
 * Struct arrays holder.
 */
public interface ArrayHolder {
    /**
     * @return The number of elements in vector. The actual reserved number of elements may be
     *         larger than that.
     */
    int size();

    /**
     * @return An array holding all integer fields. Fields are laid out sequentially with
     *         respect to field ordering specified in {@link net.nativestruct.StructField}.
     */
    int[] integers();

    /**
     * @return An array holding all double fields. Fields are laid out sequentially with respect
     * to field ordering specified in {@link net.nativestruct.StructField}.
     */
    double[] doubles();

    /**
     * @return An array holding all object fields. Fields are laid out sequentially with respect
     * to field ordering specified in {@link net.nativestruct.StructField}.
     */
    Object[] objects();

    /**
     * @return An array holding on child struct fields.
     */
    AbstractStruct[] composites();
}
