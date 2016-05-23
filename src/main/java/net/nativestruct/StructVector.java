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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.nativestruct.implementation.field.Field;
import net.nativestruct.implementation.field.Fields;
import net.nativestruct.mapreduce.InsertionReducer;
import net.nativestruct.mapreduce.Reducer;
import net.nativestruct.sorting.AbstractSortedSubstitution;
import net.nativestruct.sorting.OrderingSubstitution;
import net.nativestruct.sorting.SortedProjection;
import net.nativestruct.sorting.SortedSubstitution;

/**
 * Encapsulates a vector of structures of primitive types.
 * Internally it is organized as a number of primitive type array, holding
 * struct values in arrays.
 *
 * @param <T> Accessor type.
 */
public final class StructVector<T> implements StructProjection<T> {

    private static final int INITIAL_CAPACITY = 16;
    private static final double GROW_FACTOR = 1.5f;

    private final Fields fields;
    private final Holder holder;

    private final AbstractStruct[] accessors;
    private final T accessor;

    private int capacity;

    /**
     * Creates a new instance of struct vector given accessor interface.
     *
     * @param type     Struct accessor interface class.
     */
    public StructVector(Class<T> type) {
        this(type, INITIAL_CAPACITY);
    }

    /**
     * Creates a new instance of struct vector given accessor interface and
     * vector capacity.
     *
     * @param type     Struct accessor interface class.
     * @param capacity Initial vector capacity.
     */
    public StructVector(Class<T> type, int capacity) {
        this.fields = Fields.forType(type);
        this.accessors = buildAccessors();
        this.accessor = (T) accessors[0];
        this.holder = new Holder(fields, Arrays.asList(accessors).subList(1, accessors.length));

        reserve(capacity);
        updateAccessors();
    }

    private AbstractStruct[] buildAccessors() {
        List<AbstractStruct> accessorsList = fields.buildAccessors();
        return accessorsList.toArray(new AbstractStruct[accessorsList.size()]);
    }

    @Override
    public int size() {
        return holder.size();
    }

    @Override
    public int current() {
        return accessors[0].current();
    }

    @Override
    public void current(int index) {
        checkIndexBounds(index);
        updateCurrent(index);
    }

    private void updateCurrent(int index) {
        for (int i = 0; i < accessors.length; i++) {
            accessors[i].current(index);
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
        reserve(size);
        holder.resize(size);
        updateAccessors();
        return this;
    }

    /**
     * @param size Vector size.
     * @return New vector capacity based on the existing capacity and grow factor.
     */
    private int alignCapacity(int size) {
        return Math.max(size, (int) Math.ceil(capacity * GROW_FACTOR));
    }

    /**
     * @param name Field name.
     * @return Field instance by name.
     */
    public Field field(String name) {
        return (Field) fields.field(name);
    }

    /**
     * @return The list of the struct field names.
     */
    public List<String> allFieldNames() {
        return fields.allFieldNames();
    }

    /**
     * Inserts empty elements in vector. Subsequent elements are shifted forward.
     *
     * @param index Insertion point.
     * @param count The number of elements to insert.
     * @return This instance.
     */
    public int insert(int index, int count) {
        if (index < 0 || index > size()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int newSize = size() + count;
        if (newSize > capacity && reserve(alignCapacity(newSize))) {
            updateAccessors();
        }
        holder.insert(index, count);
        updateCurrent(index);
        return index;
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
        int newSize = index + count;
        if (newSize > capacity) {
            reserve(alignCapacity(newSize));
            updateAccessors();
        }
        holder.increaseSize(count);
        updateCurrent(index);
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
    private boolean reserve(int newCapacity) {
        assert newCapacity >= 0;
        if (newCapacity > this.capacity) {
            this.capacity = newCapacity;
            holder.reserve(newCapacity);
            return true;
        } else {
            return false;
        }
    }

    private void updateAccessors() {
        for (int i = 0; i < accessors.length; i++) {
            accessors[i].copyFrom(holder);
        }
    }

    @Override
    public T accessor() {
        return accessor;
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
     * @param field Struct field that specifies ordering.
     * @return SortedSubstitution instance.
     */
    public SortedSubstitution sortedSubstitution(Field field) {
        SortedSubstitution substitution;
        if (field.isType(int.class)) {
            substitution = holder.integerSortedSubstitution(field);
        } else if (field.isType(double.class)) {
            substitution = holder.doubleSortedSubstitution(field);
        } else if (!field.isPrimitive()) {
            //noinspection unchecked
            Comparator<Object> comparator = (left, right) -> ((Comparable) left).compareTo(right);
            substitution = holder.objectSortedSubstitution(field, comparator);
        } else {
            throw new IllegalArgumentException("Sorting is not supported for field " + field);
        }
        return substitution;
    }

    /**
     * @param field Struct field that specifies ordering.
     * @return SortedSubstitution instance.
     */
    public SortedSubstitution sortedSubstitution(String field) {
        return sortedSubstitution(field(field));
    }

    /**
     * Perform struct record sorting by the specified field.
     *
     * @param field The field that will determine the sort ordering.
     */
    public void sort(Field field) {
        holder.reorder(sortedSubstitution(field).ordering());
        updateAccessors();
    }

    /**
     * Perform struct record sorting by the specified field.
     *
     * @param name Name of the field used for sorting.
     */
    public void sort(String name) {
        Field field = field(name);
        if (field == null) {
            throw new IllegalArgumentException("Unknown field: " + name);
        }
        sort(field);
    }

    /**
     * Perform struct record sorting by the specified object field.
     *
     * @param field Object field used for sorting.
     * @param comparator Comparator used for the field values comparison.
     * @param <U> Type of the field being sorted.
     */
    public <U> void sort(Field field, Comparator<U> comparator) {
        holder.reorder(holder.objectSortedSubstitution(field, comparator).ordering());
        updateAccessors();
    }

    /**
     * Perform struct record sorting by the specified object field.
     *
     * @param name Name of the field used for sorting.
     * @param comparator Comparator used for the field values comparison.
     * @param <U> Type of the field being sorted.
     */
    public <U> void sort(String name, Comparator<U> comparator) {
        Field field = field(name);
        if (field == null) {
            throw new IllegalArgumentException("Unknown field: " + name);
        }
        sort(field, comparator);
    }

    /**
     * Creates a projection with records ordered by the specified field.
     * The original vector stays intact.
     *
     * @param name Name of the field.
     * @return Project instance.
     */
    public StructProjection<T> asSorted(String name) {
        return new SortedProjection<>(this, field(name));
    }

    /**
     * Starts building reduce operation by the given field.
     *
     * @param field Field name for which the reduce operation will be performed.
     * @return New reducer builder object.
     */
    public Reducer<T> reduceBy(String field) {
        return new InsertionReducer<T>(this, field);
    }

    /**
     * Updates one record in the current vector by copying a record from source vector.
     * Both vectors should represent the same struct.
     *
     * @param targetIndex Index of the record in the current struct vector being updated.
     * @param source Source struct vector.
     * @param sourceIndex Index of the record being copied.
     */
    public void updateFrom(int targetIndex, StructVector<T> source, int sourceIndex) {
        int intFields = fields.intFields();
        if (intFields > 0) {
            System.arraycopy(source.integers(), sourceIndex * intFields,
                    integers(), targetIndex * intFields, intFields);
        }

        int doubleFields = fields.doubleFields();
        if (doubleFields > 0) {
            System.arraycopy(source.doubles(), sourceIndex * doubleFields,
                    doubles(), targetIndex * doubleFields, doubleFields);
        }

        int objectFields = fields.objectFields();
        if (objectFields > 0) {
            System.arraycopy(source.objects(), sourceIndex * objectFields,
                    objects(), targetIndex * objectFields, objectFields);
        }
    }

    /**
     * Retrieve integer field value.
     *
     * @param field Field object.
     * @param index Record index.
     * @return Integer value of the field.
     */
    public int fieldValueInteger(Field field, int index) {
        checkIndexBounds(index);
        return accessors[0].intFieldIndexed(fields.intFields(), index, field.index());
    }

    /**
     * Retrieve double field value.
     *
     * @param field Field object.
     * @param index Record index.
     * @return Double value of the field.
     */
    public double fieldValueDouble(Field field, int index) {
        checkIndexBounds(index);
        return accessors[0].doubleFieldIndexed(fields.doubleFields(), index, field.index());
    }

    /**
     * Retrieve object field value.
     *
     * @param field Field object.
     * @param index Record index.
     * @return Object value of the field.
     */
    public Object fieldValueObject(Field field, int index) {
        checkIndexBounds(index);
        return accessors[0].objectFieldIndexed(fields.objectFields(), index, field.index());
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
         * @param capacity The number of elements in array.
         */
        final void reserve(int capacity) {
            reserveIntegers(capacity, fields.intFields());
            reserveDoubles(capacity, fields.doubleFields());
            reserveObjects(capacity, fields.objectFields());
        }

        /**
         * Inserts empty elements in vector. Subsequent elements are shifted forward.
         *
         * @param index Insertion point.
         * @param count The number of elements to insert.
         */
        final void insert(int index, int count) {
            if (index < size) {
                int intFields = fields.intFields();
                if (intFields > 0) {
                    insertInArray(index, count, this.integers, intFields);
                    for (int i = 0; i < count * intFields; i++) {
                        integers[index * intFields + i] = 0;
                    }
                }

                int doubleFields = fields.doubleFields();
                if (doubleFields > 0) {
                    insertInArray(index, count, this.doubles, doubleFields);
                    for (int i = 0; i < count * doubleFields; i++) {
                        doubles[index * doubleFields + i] = 0;
                    }
                }

                int objectFields = fields.objectFields();
                if (objectFields > 0) {
                    insertInArray(index, count, this.objects, objectFields);
                    for (int i = 0; i < count * objectFields; i++) {
                        objects[index * objectFields + i] = null;
                    }
                }
            }

            increaseSize(count);
        }

        private void increaseSize(int count) {
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
            if (newSize < this.size) {
                int intFields = fields.intFields();
                if (intFields > 0) {
                    for (int i = newSize * intFields; i < size * intFields; i++) {
                        integers[i] = 0;
                    }
                }

                int doubleFields = fields.doubleFields();
                if (doubleFields > 0) {
                    for (int i = newSize * doubleFields; i < size * doubleFields; i++) {
                        doubles[i] = 0.0;
                    }
                }

                int objectFields = fields.objectFields();
                if (objectFields > 0) {
                    for (int i = newSize * objectFields; i < size * objectFields; i++) {
                        objects[i] = null;
                    }
                }
            }
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

        private SortedSubstitution integerSortedSubstitution(Field field) {
            return new AbstractSortedSubstitution.Integers(
                    integers, size, fields.intFields(), field.index());
        }

        private SortedSubstitution doubleSortedSubstitution(Field field) {
            return new AbstractSortedSubstitution.Doubles(
                    doubles, size, fields.doubleFields(), field.index());
        }

        private <U> SortedSubstitution objectSortedSubstitution(
                Field field, Comparator<U> comparator) {
            return new AbstractSortedSubstitution.Objects(
                    objects, size, fields.objectFields(), field.index(), comparator);
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
