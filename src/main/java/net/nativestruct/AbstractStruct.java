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
 * Base class for all accessor interfaces. It encapsulates primitive arrays
 * holding struct field values.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractStruct {
    private int         size;
    private int[]       integers;
    private double[]    doubles;
    private Object[]    objects;

    private AbstractStruct[]    composites;
    private int                 current = -1;

    /**
     * @return Current record index. By default it equals -1.
     */
    protected final int current() {
        return current;
    }

    /**
     * Updates the current record index.
     *
     * @param index A new record index.
     */
    final void current(int index) {
        this.current = index;
    }

    /**
     * Checks that the index falls within vector bounds. Method is called from dynamically
     * generated field accessors.
     *
     * @param index Index in vector.
     */
    protected final void checkIndexBounds(int index) {
        if (index < 0 || index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    /**
     * Copies field array from a source array holder.
     *
     * @param holder Array holder.
     */
    protected final void copyFrom(ArrayHolder holder) {
        this.size = holder.size();
        this.integers = holder.integers();
        this.doubles = holder.doubles();
        this.objects = holder.objects();
        this.composites = holder.composites();
        this.current = -1;
    }

    /**
     * Getter method of child struct field.
     *
     * @param index Child struct index.
     * @return Child struct accessor object.
     */
    protected final AbstractStruct composite(int index) {
        return this.composites[index];
    }

    /**
     * Getter method of integer struct field.
     *
     * @param fields The number of integer fields in the struct.
     * @param index  Struct index in the array.
     * @param field  Field index among all the integer fields.
     * @return Integer field value.
     */
    protected final int intFieldIndexed(int fields, int index, int field) {
        return integers[index * fields + field];
    }

    /**
     * Setter method of integer struct field.
     *
     * @param fields The number of integer fields in the struct.
     * @param index  Struct index in the array.
     * @param field  Field index among all the integer fields.
     * @param value  Value to be set.
     */
    protected final void updateIntFieldIndexed(int fields, int index, int field, int value) {
        integers[index * fields + field] = value;
    }

    /**
     * Getter method of double struct field.
     *
     * @param fields The number of double fields in the struct.
     * @param index  Struct index in the array.
     * @param field  Field index among all the double fields.
     * @return Double field value.
     */
    protected final double doubleFieldIndexed(int fields, int index, int field) {
        return doubles[index * fields + field];
    }

    /**
     * Setter method of double struct field.
     *
     * @param fields The number of double fields in the struct.
     * @param index  Struct index in the array.
     * @param field  Field index among all the double fields.
     * @param value  Value to be set.
     */
    protected final void updateDoubleFieldIndexed(int fields, int index, int field, double value) {
        doubles[index * fields + field] = value;
    }

    /**
     * Getter method of Object struct field.
     *
     * @param fields The number of Object fields in the struct.
     * @param index  Struct index in the array.
     * @param field  Field index among all the Object fields.
     * @return Object field value.
     */
    protected final Object objectFieldIndexed(int fields, int index, int field) {
        return objects[index * fields + field];
    }

    /**
     * Setter method of Object struct field.
     *
     * @param fields The number of Object fields in the struct.
     * @param index  Struct index in the array.
     * @param field  Field index among all the Object fields.
     * @param value  Value to be set.
     */
    protected final void updateObjectFieldIndexed(int fields, int index, int field, Object value) {
        objects[index * fields + field] = value;
    }
}
