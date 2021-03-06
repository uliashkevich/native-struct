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

import java.util.function.Supplier;

/**
 * Specifies ordering of fields with the same type within an array.
 */
public final class FieldOrdering implements Comparable<FieldOrdering> {
    private String name;
    private int order = -1;
    private Supplier<FieldLike> supplier;

    /**
     * Constructs ordering instance given a field name.
     * @param name Field name.
     * @param supplier Supplier of field instance.
     *
     */
    public FieldOrdering(String name, Supplier<FieldLike> supplier) {
        this.name = name;
        this.supplier = supplier;
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
     * Creates {@link net.nativestruct.implementation.field.Field} instance with the current field
     * index.
     *
     * @return New field instance.
     */
    public FieldLike createField() {
        return supplier.get();
    }

    @Override
    public int hashCode() {
        return 31 * order + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FieldOrdering && compareTo((FieldOrdering) obj) == 0;
    }

    @Override
    public int compareTo(FieldOrdering other) {
        int comparison = Integer.compare(order, other.getOrder());
        return comparison == 0
                ? name.compareTo(other.getName())
                : comparison;
    }
}
