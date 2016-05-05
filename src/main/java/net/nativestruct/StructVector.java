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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.nativestruct.implementation.field.Fields;

/**
 * Encapsulates a vector of structures of primitive types.
 * Internally it is organized as a number of primitive type array, holding
 * struct values in arrays.
 *
 * @param <T> Accessor type.
 */
public final class StructVector<T> {

    private Class<T> type;

    private Fields fields;
    private Holder holder;

    private T accessor;
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
        this.type = type;
        this.fields = new Fields(type);
        this.accessor = buildAccessor();
        this.accessors = new ArrayList<>(Arrays.asList(struct()));
        this.holder = new Holder();

        reserve(capacity);
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
     * Inserts empty elements in vector. Subsequent elements are shifted forward.
     *
     * @param index Insertion point.
     * @param count The number of elements to insert.
     * @return This instance.
     */
    public StructVector<T> insert(int index, int count) {
        if (index < 0 || index > holder.size()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        reserve(capacity + count);
        holder.insert(index, count);
        updateAccessors();
        return this;
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

    private AbstractStruct struct() {
        return (AbstractStruct) accessor;
    }

    @SuppressWarnings("unchecked")
    private T buildAccessor() {
        try {
            DynamicType.Builder<AbstractStruct> bareStruct =
                    AbstractStruct.class.isAssignableFrom(this.type)
                            ? new ByteBuddy().subclass((Class<AbstractStruct>) this.type)
                            : new ByteBuddy().subclass(AbstractStruct.class).implement(this.type);

            return (T) fields.installAccessors(bareStruct)
                    .make()
                    .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot create accessor for " + this.type, e);
        }
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
     * Holds internal arrays for storing struct fields.
     */
    public class Holder implements ArrayHolder {

        private int         size;
        private int[]       integers;
        private double[]    doubles;
        private Object[]    objects;

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
            insertInArray(index, count, this.integers(), intFields);
            for (int i = 0; i < count; i++) {
                this.integers()[(index + i) * intFields] = 0;
            }

            int doubleFields = fields.doubleFields();
            insertInArray(index, count, this.doubles(), doubleFields);
            for (int i = 0; i < count; i++) {
                this.doubles()[(index + i) * doubleFields] = 0;
            }

            int objectFields = fields.objectFields();
            insertInArray(index, count, this.objects(), objectFields);
            for (int i = 0; i < count; i++) {
                this.objects()[(index + i) * objectFields] = null;
            }

            size += count;
        }

        private void insertInArray(int index, int count, Object array, int width) {
            System.arraycopy(array, index * width,
                    array, (index + count) * width, (size() - index) * width);
        }

        private void reserveIntegers(int newSize, int intFields) {
            if (intFields > 0) {
                int[] newIntegers = new int[newSize * intFields];
                if (this.integers() != null) {
                    System.arraycopy(integers(), 0, newIntegers, 0,
                            Math.min(integers().length, newIntegers.length));
                }
                this.integers = newIntegers;
            }
        }

        private void reserveDoubles(int newSize, int doubleFields) {
            if (doubleFields > 0) {
                double[] newDoubles = new double[newSize * doubleFields];
                if (doubles() != null) {
                    System.arraycopy(doubles(), 0, newDoubles, 0,
                            Math.min(doubles().length, newDoubles.length));
                }
                this.doubles = newDoubles;
            }
        }

        private void reserveObjects(int newSize, int objectFields) {
            if (objectFields > 0) {
                Object[] newObjects = new Object[newSize * objectFields];
                if (objects() != null) {
                    System.arraycopy(objects(), 0, newObjects, 0,
                            Math.min(objects().length, newObjects.length));
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

        /**
         * @return The number of elements in
         */
        @Override
        public final int size() {
            return this.size;
        }

        /**
         * @return An array holding all integer fields. Fields are laid out sequentially with
         *         respect to field ordering specified in {@link net.nativestruct.StructField}.
         */
        @Override
        public final int[] integers() {
            return integers;
        }

        /**
         * @return An array holding all double fields. Fields are laid out sequentially with respect
         * to field ordering specified in {@link net.nativestruct.StructField}.
         */
        @Override
        public final double[] doubles() {
            return doubles;
        }

        /**
         * @return An array holding all object fields. Fields are laid out sequentially with respect
         * to field ordering specified in {@link net.nativestruct.StructField}.
         */
        @Override
        public final Object[] objects() {
            return objects;
        }
    }
}
