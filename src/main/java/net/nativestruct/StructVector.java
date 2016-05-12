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

import java.util.Comparator;
import java.util.List;

import net.nativestruct.implementation.field.Field;
import net.nativestruct.implementation.field.Fields;
import net.nativestruct.implementation.field.FieldsBuilder;
import net.nativestruct.sorting.AbstractSortedSubstitution;
import net.nativestruct.sorting.OrderingSubstitution;

/**
 * Encapsulates a vector of structures of primitive types.
 * Internally it is organized as a number of primitive type array, holding
 * struct values in arrays.
 *
 * @param <T> Accessor type.
 */
public final class StructVector<T> {

    private Fields fields;
    private Holder holder;

    private List<AbstractStruct> accessors;

    private int capacity;

    /**
     * Creates a new instance of struct vector given accessor interface and
     * vector capacity.
     *
     * @param type     Struct accessor interface class.
     * @param capacity Initial vector capacity.
     */
    public StructVector(Class<T> type, int capacity) {
        this.fields = new FieldsBuilder(type).build();
        this.accessors = fields.buildAccessors();
        this.holder = new Holder(fields, accessors.subList(1, accessors.size()));

        reserve(capacity);
    }

    /**
     * @return Vector size.
     */
    public int size() {
        return holder.size();
    }

    /**
     * @return Current record index. By default it equals -1.
     */
    public int current() {
        return accessors.get(0).current();
    }

    /**
     * Updates the current record index for all the field accessors and enclosed struct records.
     *
     * @param index A new record index.
     */
    void current(int index) {
        checkIndexBounds(index);
        for (AbstractStruct accessor : accessors) {
            accessor.current(index);
        }
    }

    /**
     * Checks that the index falls within vector bounds.
     *
     * @param index Index in vector.
     */
    protected void checkIndexBounds(int index) {
        if (index < 0 || index >= size()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    /**
     * Changes the size of the vector. Allocates internal buffer at least the specified size.
     *
     * @param size Vector size.
     * @return This instance.
     */
    public StructVector<T> resize(int size) {
        reserve(ceilingPowerOfTwo(size));
        holder.resize(size);
        updateAccessors();
        return this;
    }

    /**
     * @param size Vector size.
     * @return Nearest power of two larger than the size.
     */
    private int ceilingPowerOfTwo(int size) {
        int num = size - 1;
        num |= num >> 1;
        num |= num >> 2;
        num |= num >> 4;
        num |= num >> 8;
        num |= num >> 16;
        return num + 1;
    }

    /**
     * @param name Field name.
     * @return Field instance by name.
     */
    public Field field(String name) {
        return (Field) fields.field(name);
    }

    /**
     * Inserts empty elements in vector. Subsequent elements are shifted forward.
     *
     * @param index Insertion point.
     * @param count The number of elements to insert.
     * @return This instance.
     */
    public StructVector<T> insert(int index, int count) {
        if (index < 0 || index > size()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        reserve(ceilingPowerOfTwo(size() + count));
        holder.insert(index, count);
        updateAccessors();
        return this;
    }

    /**
     * Inserts empty elements at the end of the vector.
     *
     * @param count The number of items to insert.
     * @return Index of the first inserted element.
     */
    public int insertLast(int count) {
        assert count > 0;
        int index = size();
        reserve(ceilingPowerOfTwo(index + count));
        holder.insert(index, count);
        updateAccessors();
        return index;
    }

    /**
     * Inserts empty elements at the end of the vector.
     *
     * @return Index of the inserted element.
     */
    public int insertLast() {
        return insertLast(1);
    }

    /**
     * Reallocate internal arrays to hold the specified number of structs.
     *
     * @param newCapacity The number of elements.
     */
    private StructVector<T> reserve(int newCapacity) {
        assert newCapacity >= 0;
        if (newCapacity > this.capacity) {
            this.capacity = newCapacity;
            holder.reserve(newCapacity);
        }
        return this;
    }

    private void updateAccessors() {
        for (AbstractStruct acc : accessors) {
            acc.copyFrom(holder);
        }
    }

    /**
     * @return Accessor instance, giving access to vector elements.
     */
    public T accessor() {
        return (T) this.accessors.get(0);
    }

    /**
     * @return An array holding all integer fields.
     */
    public int[] integers() {
        return holder.integers();
    }

    /**
     * @return An array holding all double fields.
     */
    public double[] doubles() {
        return holder.doubles();
    }

    /**
     * @return An array holding all object fields.
     */
    public Object[] objects() {
        return holder.objects();
    }

    /**
     * Generate new records and put them into the struct array.
     *
     * @param source    The source struct vector, based off which the records are generated.
     * @param generator The generator interface, holding method that is executed for each record.
     *                  from the source struct vector.
     * @param <U>       Type of the source struct vector.
     * @param <V>       Type of the generator interface.
     */
    public <U, V> void generate(StructVector<U> source, V generator) {
        //source.iterateFields(this, new InnerAdder(), generator);
    }

    /**
     * Searches the struct vector for the specified value using the binary search algorithm.
     * The vector elements should be sorted prior to making the call. If the array contains
     * multiple elements with the specified value, there is no guarantee which one will be
     * found.
     *
     * @param field Struct field to be search through.
     * @param value The value to be searched for.
     * @return Index of the struct record, if it is contained in the vector.
     *         Otherwise <tt>(-(<i>insertion point</i>) - 1)</tt>.
     */
    public int binarySearch(Field field, int value) {
        assert fields.hasField(field);
        assert field.isType(int.class);
        return holder.binarySearch(fields.intFields(), field.index(), value);
    }

    /**
     * Searches the struct vector for the specified value using the binary search algorithm.
     * The vector elements should be sorted prior to making the call. If the array contains
     * multiple elements with the specified value, there is no guarantee which one will be
     * found.
     *
     * @param field Struct field to be search through.
     * @param value The value to be searched for.
     * @return Index of the struct record, if it is contained in the vector.
     *         Otherwise <tt>(-(<i>insertion point</i>) - 1)</tt>.
     */
    public int binarySearch(Field field, double value) {
        assert fields.hasField(field);
        assert field.isType(double.class);
        return holder.binarySearch(fields.doubleFields(), field.index(), value);
    }

    /**
     * Searches the struct vector for the specified value using the binary search algorithm.
     * The vector elements should be sorted prior to making the call. If the array contains
     * multiple elements with the specified value, there is no guarantee which one will be
     * found.
     *
     * @param field Struct field to be search through.
     * @param value The value to be searched for.
     * @return Index of the struct record, if it is contained in the vector.
     *         Otherwise <tt>(-(<i>insertion point</i>) - 1)</tt>.
     */
    public int binarySearch(Field field, Comparable<?> value) {
        assert fields.hasField(field);
        assert field.isType(value.getClass());
        return holder.binarySearch(fields.objectFields(), field.index(), value);
    }

    /**
     * Perform struct record sorting by the specified field.
     *
     * @param field The field that will determine the sort ordering.
     */
    public void sort(Field field) {
        if (field.isType(int.class)) {
            holder.integerSort(fields.intFields(), field.index());
        } else if (field.isType(double.class)) {
            holder.doubleSort(fields.doubleFields(), field.index());
        } else if (!field.isPrimitive()) {
            //noinspection unchecked
            holder.objectSort(fields.objectFields(), field.index(),
                (left, right) -> ((Comparable) left).compareTo(right));
        } else {
            throw new IllegalArgumentException("Sorting is not supported for field " + field);
        }
        updateAccessors();
    }

    /**
     * Perform struct record sorting by the specified object field.
     *
     * @param field Object field used for sorting.
     * @param comparator Comparator used for the field values comparison.
     * @param <U> Type of the field being sorted.
     */
    public <U> void sort(Field field, Comparator<U> comparator) {
        holder.objectSort(fields.objectFields(), field.index(), comparator);
        updateAccessors();
    }

    /**
     * Holds internal arrays for storing struct fields.
     */
    public static class Holder implements ArrayHolder {

        private Fields      fields;
        private int         size;
        private int[]       integers;
        private double[]    doubles;
        private Object[]    objects;

        private AbstractStruct[]    composites;

        /**
         * Construct internal arrays holder.
         *
         * @param fields Fields description.
         * @param accessors A list of fields accessors.
         */
        public Holder(Fields fields, List<AbstractStruct> accessors) {
            this.fields = fields;
            if (fields.composites() > 0) {
                this.composites = new AbstractStruct[fields.composites()];
                for (int i = 0; i < composites.length; i++) {
                    composites[i] = accessors.get(i);
                }
            }
        }

        /**
         * Reallocates internal array for all fields of the struct.
         * @param newSize The number of elements in array.
         */
        final void reserve(int newSize) {
            reserveIntegers(newSize, fields.intFields());
            reserveDoubles(newSize, fields.doubleFields());
            reserveObjects(newSize, fields.objectFields());
        }

        /**
         * Inserts empty elements in vector. Subsequent elements are shifted forward.
         *
         * @param index Insertion point.
         * @param count The number of elements to insert.
         */
        final void insert(int index, int count) {
            int intFields = fields.intFields();
            if (index < size) {
                insertInArray(index, count, this.integers, intFields);
            }
            for (int i = 0; i < count; i++) {
                integers[(index + i) * intFields] = 0;
            }

            int doubleFields = fields.doubleFields();
            if (index < size) {
                insertInArray(index, count, this.doubles, doubleFields);
            }
            for (int i = 0; i < count; i++) {
                doubles[(index + i) * doubleFields] = 0;
            }

            int objectFields = fields.objectFields();
            if (index < size) {
                insertInArray(index, count, this.objects, objectFields);
            }
            for (int i = 0; i < count; i++) {
                objects[(index + i) * objectFields] = null;
            }

            size += count;
        }

        @SuppressWarnings("SuspiciousSystemArraycopy")
        private void insertInArray(int index, int count, Object array, int width) {
            System.arraycopy(array, index * width,
                    array, (index + count) * width, (size() - index) * width);
        }

        private void reserveIntegers(int newSize, int intFields) {
            if (intFields > 0) {
                int[] newIntegers = new int[newSize * intFields];
                if (this.integers != null) {
                    System.arraycopy(this.integers, 0, newIntegers, 0,
                            Math.min(this.integers.length, newIntegers.length));
                }
                this.integers = newIntegers;
            }
        }

        private void reserveDoubles(int newSize, int doubleFields) {
            if (doubleFields > 0) {
                double[] newDoubles = new double[newSize * doubleFields];
                if (doubles != null) {
                    System.arraycopy(doubles, 0, newDoubles, 0,
                            Math.min(doubles.length, newDoubles.length));
                }
                this.doubles = newDoubles;
            }
        }

        private void reserveObjects(int newSize, int objectFields) {
            if (objectFields > 0) {
                Object[] newObjects = new Object[newSize * objectFields];
                if (objects != null) {
                    System.arraycopy(objects, 0, newObjects, 0,
                            Math.min(objects.length, newObjects.length));
                }
                this.objects = newObjects;
            }
        }

        /**
         * Updates internal size.
         *
         * @param newSize Vector size.
         */
        final void resize(int newSize) {
            this.size = newSize;
        }

        @Override
        public final int size() {
            return this.size;
        }

        @Override
        public final int[] integers() {
            return integers;
        }

        @Override
        public final double[] doubles() {
            return doubles;
        }

        @Override
        public final Object[] objects() {
            return objects;
        }

        @Override
        public final AbstractStruct[] composites() {
            return composites;
        }

        /**
         * Searches the struct vector for the specified value using the binary search algorithm.
         * The vector elements should be sorted prior to making the call. If the array contains
         * multiple elements with the specified value, there is no guarantee which one will be
         * found.
         *
         * @param rowWidth The number of rowWidth of the same type.
         * @param index Field index within a typed array.
         * @param value The value to be searched for.
         * @return Index of the struct record, if it is contained in the vector.
         *         Otherwise <tt>(-(<i>insertion point</i>) - 1)</tt>.
         */
        final int binarySearch(int rowWidth, int index, int value) {
            int low = 0;
            int high = size - 1;

            while (low <= high) {
                int middle = (low + high) >>> 1;
                int middleValue = integers[middle * rowWidth + index];

                if (middleValue < value) {
                    low = middle + 1;
                } else if (middleValue > value) {
                    high = middle - 1;
                } else {
                    return middle;
                }
            }
            return -(low + 1);
        }

        /**
         * Searches the struct vector for the specified value using the binary search algorithm.
         * The vector elements should be sorted prior to making the call. If the array contains
         * multiple elements with the specified value, there is no guarantee which one will be
         * found.
         *
         * @param rowWidth The number of field of the same type.
         * @param index Field index within a typed array.
         * @param value The value to be searched for.
         * @return Index of the struct record, if it is contained in the vector.
         *         Otherwise <tt>(-(<i>insertion point</i>) - 1)</tt>.
         */
        final int binarySearch(int rowWidth, int index, double value) {
            int low = 0;
            int high = size - 1;

            while (low <= high) {
                int middle = (low + high) >>> 1;
                double middleValue = doubles[middle * rowWidth + index];

                if (middleValue < value) {
                    low = middle + 1;
                } else if (middleValue > value) {
                    high = middle - 1;
                } else {
                    long middleBits = Double.doubleToLongBits(middleValue);
                    long valueBits = Double.doubleToLongBits(value);
                    if (middleBits == valueBits) {
                        return middle;
                    } else if (middleBits < valueBits) {
                        // (-0.0, 0.0) or (!NaN, NaN)
                        low = middle + 1;
                    } else {
                        // (0.0, -0.0) or (NaN, !NaN)
                        high = middle - 1;
                    }
                }
            }
            return -(low + 1);
        }

        /**
         * Searches the struct vector for the specified value using the binary search algorithm.
         * The vector elements should be sorted prior to making the call. If the array contains
         * multiple elements with the specified value, there is no guarantee which one will be
         * found.
         *
         * @param <U> Field type.
         * @param rowWidth The number of field of the same type.
         * @param index Field index within a typed array.
         * @param value The value to be searched for.
         * @return Index of the struct record, if it is contained in the vector.
         *         Otherwise <tt>(-(<i>insertion point</i>) - 1)</tt>.
         */
        final <U> int binarySearch(int rowWidth, int index, Comparable<U> value) {
            int low = 0;
            int high = size - 1;

            while (low <= high) {
                int middle = (low + high) >>> 1;
                @SuppressWarnings("unchecked")
                U middleValue = (U) objects[middle * rowWidth + index];
                int comparison = value.compareTo(middleValue);

                if (comparison > 0) {
                    low = middle + 1;
                } else if (comparison < 0) {
                    high = middle - 1;
                } else {
                    return middle;
                }
            }
            return -(low + 1);
        }

        /**
         * Sort by integer field.
         *
         * @param intFields The number of integer fields.
         * @param fieldIndex Field index.
         */
        public final void integerSort(int intFields, int fieldIndex) {
            reorder(new AbstractSortedSubstitution.Integers(
                    integers, size, intFields, fieldIndex).substitution());
        }

        /**
         * Sort by double field.
         *
         * @param doubleFields The number of double fields.
         * @param fieldIndex Field index.
         */
        public final void doubleSort(int doubleFields, int fieldIndex) {
            reorder(new AbstractSortedSubstitution.Doubles(
                    doubles, size, doubleFields, fieldIndex).substitution());
        }

        /**
         * Sort by object field.
         *
         * @param objectFields The number of object fields.
         * @param fieldIndex Field index.
         * @param comparator Comparator used for the field values comparison.
         * @param <U> Comparator generic type.
         */
        public final <U> void objectSort(
                int objectFields, int fieldIndex, Comparator<U> comparator) {
            reorder(new AbstractSortedSubstitution.Objects(
                    objects, size, objectFields, fieldIndex, comparator).substitution());
        }

        /**
         * Reorder struct records by using substitution index array.
         *
         * @param substitution Substitution index array.
         */
        private void reorder(OrderingSubstitution substitution) {
            substitution.reorder(this::swapRows);
        }

        /**
         * Swap two struct records specified by their indexes.
         *
         * @param first First record index.
         * @param second Second record index.
         */
        private void swapRows(int first, int second) {
            int intFields = fields.intFields();
            for (int i = 0; i < intFields; i++) {
                int temp = integers[first * intFields + i];
                integers[first * intFields + i] = integers[second * intFields + i];
                integers[second * intFields + i] = temp;
            }

            int doubleFields = fields.doubleFields();
            for (int i = 0; i < doubleFields; i++) {
                double temp = doubles[first * doubleFields + i];
                doubles[first * doubleFields + i] = doubles[second * doubleFields + i];
                doubles[second * doubleFields + i] = temp;
            }

            int objectFields = fields.objectFields();
            for (int i = 0; i < objectFields; i++) {
                Object temp = objects[first * objectFields + i];
                objects[first * objectFields + i] = objects[second * objectFields + i];
                objects[second * objectFields + i] = temp;
            }
        }
    }
}
