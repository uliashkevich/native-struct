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

package net.nativestruct.implementation.field;

/**
 * Field counter.
 */
public final class FieldCounter implements FieldCounts {
    private int integers;
    private int doubles;
    private int objects;

    private int composites;

    @Override
    public int fieldsOf(Class<?> type) {
        int count;
        if (type == int.class) {
            count = integers;
        } else if (type == double.class) {
            count = doubles;
        } else if (!type.isPrimitive()) {
            count = objects;
        } else {
            throw new AssertionError("Unsupported type: " + type);
        }
        return count;
    }

    /**
     * @return The number of child struct fields.
     */
    public int composites() {
        return composites;
    }

    /**
     * Increments the number of fields of a given type and returns the current field index.
     *
     * @param type Primitive type.
     * @return New field index of a given type.
     */
    public int increment(Class<?> type) {
        int index;
        if (type == int.class) {
            index = integers++;
        } else if (type == double.class) {
            index = doubles++;
        } else if (!type.isPrimitive()) {
            index = objects++;
        } else {
            throw new AssertionError("Unsupported type: " + type);
        }
        return index;
    }

    /**
     * Increments the number of child struct fields and returns the current index.
     *
     * @return New child struct index.
     */
    public int incrementComposites() {
        return composites++;
    }
}
