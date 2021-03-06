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

/**
 * Keeps elements substitutions to perform reordering.
 */
public interface OrderingSubstitution {
    /**
     * @param index Input index.
     * @return Index after applying substitution.
     */
    int forIndex(int index);

    /**
     * Performs reordering of elements.
     *
     * @param operation Callback interface for swapping elements.
     */
    void reorder(SubstitutionOp operation);

    /**
     * Swapping elements functional interface.
     */
    @FunctionalInterface
    public interface SubstitutionOp {
        /**
         * Swap two elements in array specified by their indexes.
         * @param first Index of the first element.
         * @param second Index of the second element.
         */
        void swap(int first, int second);
    }
}
