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
 * Keeps two arrays - direct and inverse indexes substitution.
 */
class IndexBasedSubstitution implements OrderingSubstitution {
    private int[] first;
    private int[] second;

    /**
     * Constructs instance.
     *
     * @param first Direct index substitution array. Element in i-th position points to an array
     *              record that should be placed in that position.
     * @param second Inverse index substitution arrays. Element in i-th position points to a place
     *               in an array, where the i-th record should be moved.
     */
    IndexBasedSubstitution(int[] first, int[] second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void reorder(SubstitutionOp operation) {
        for (int i = 0; i < first.length; i++) {
            operation.swap(i, first[i]);
            first[second[i]] = first[i];
            second[first[i]] = second[i];
        }
    }
}
