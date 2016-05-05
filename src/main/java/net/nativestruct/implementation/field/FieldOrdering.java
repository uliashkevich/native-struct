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

import java.util.List;

/**
 * Specifies ordering of fields with the same type within an array.
 */
public final class FieldOrdering {
    private String name;
    private Class<?> type;
    private List<Accessor> accessors;
    private int order = -1;

    /**
     * Constructs ordering instance given a field name.
     *  @param name Field name.
     * @param type Field type.
     * @param accessors The list of field accessors.
     */
    public FieldOrdering(String name, Class<?> type, List<Accessor> accessors) {
        this.name = name;
        this.type = type;
        this.accessors = accessors;
    }

    /**
     * @return Field name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns calculated ordering. Valid after executing {@link #updateOrder(int)} method
     *         for all the accessors.
     */
    public int getOrder() {
        return order;
    }

    /**
     * Updates the order.
     *
     * @param newOrder Ordering number specified for one of the field accessors.
     * @return If the order was updated successfully. Otherwise it means the ordering is ambiguous.
     */
    public boolean updateOrder(int newOrder) {
        boolean result = true;
        if (order != newOrder) {
            if (order < 0 || newOrder < 0) {
                order = Math.max(order, newOrder);
            } else {
                result = false;
            }
        }
        return result;
    }

    /**
     * Assigns field index taking into account the field type.
     *
     * @param counter Field index counter object.
     * @return The assigned index.
     */
    public int assignIndex(FieldCounter counter) {
        return counter.increment(type);
    }

    /**
     * Creates {@link net.nativestruct.implementation.field.Field} instance with the current field index.
     *
     * @param counter Counter instance, which holds the number of struct fields of each type.
     * @return New field instance.
     */
    public Field createFieldWithIndex(FieldCounter counter) {
        return new Field(assignIndex(counter), counter, accessors);
    }
}
