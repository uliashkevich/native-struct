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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.nativestruct.AbstractStruct;
import net.nativestruct.StructField;

/**
 * {@link net.nativestruct.implementation.field.Fields} builder object.
 */
public class FieldsBuilder {

    private final Class<?> type;
    private final FieldCounter counter;
    private final List<Accessor> accessors;
    private final int index;

    /**
     * Constructs builder instance.
     *
     * @param type Struct type.
     * @param index Composite index, or -1 or it is root struct.
     * @param counter Field type counter object.
     * @param accessors The list of the fields accessors, or empty if it is root struct.
     */
    private FieldsBuilder(Class<?> type, int index, FieldCounter counter,
                          List<Accessor> accessors) {
        this.type = type;
        this.counter = counter;
        this.accessors = accessors;
        this.index = index;
    }

    /**
     * Constructs {@link net.nativestruct.implementation.field.Fields} builder for root struct.
     *
     * @param type Struct type.
     */
    public FieldsBuilder(Class<?> type) {
        this(type, -1, new FieldCounter(), Collections.emptyList());
    }

    /**
     * @return A new {@link net.nativestruct.implementation.field.Fields} builder instance.
     */
    public final Fields build() {
        return new Fields(type, index, counter, accessors,
                buildFields(buildFieldOrdering(buildAccessorTypes())));
    }

    private List<FieldOrdering> buildFieldOrdering(Map<String, List<Accessor>> accessorTypes) {

        Map<String, FieldOrdering> orderings = new HashMap<>();
        forEachAccessor((method, annotation) -> {
            String fieldName = propertyName(method, annotation);
            FieldOrdering ordering = orderings.computeIfAbsent(fieldName,
                name -> createFieldOrdering(name, accessorTypes.get(name)));
            if (!ordering.updateOrder(annotation.order())) {
                throw new AssertionError(String.format(
                        "Ambiguous field ordering: %s.%s", type.getSimpleName(), fieldName));
            }
        });

        return orderings.values().stream().sorted().collect(Collectors.toList());
    }

    private FieldOrdering createFieldOrdering(String name, List<Accessor> fieldAccessors) {
        Class<?> propertyType = fieldAccessors.get(0).getType();
        if (AbstractStruct.class.isAssignableFrom(propertyType)) {
            return new FieldOrdering(name, () -> new FieldsBuilder(
                    propertyType, counter.incrementComposites(), counter, fieldAccessors)
                    .build());
        } else {
            return new FieldOrdering(name, () -> new Field(name, propertyType,
                counter.increment(propertyType), counter, fieldAccessors));
        }
    }

    private Map<String, List<Accessor>> buildAccessorTypes() {
        Map<String, List<Accessor>> result = new HashMap<>();
        forEachAccessor((method, annotation) -> {
            List<Accessor> fieldAccessors = result.computeIfAbsent(
                    propertyName(method, annotation), name -> new ArrayList<>());
            Accessor accessor = AbstractAccessor.of(annotation.accessor(), method);
            if (!fieldAccessors.isEmpty()) {
                accessor.checkCompatibility(fieldAccessors.get(0));
            }
            fieldAccessors.add(accessor);
        });
        return result;
    }

    private Map<String, FieldLike> buildFields(List<FieldOrdering> orderings) {
        Map<String, FieldLike> namedFields = new LinkedHashMap<>();
        orderings.stream().forEach(
            ordering -> namedFields.put(ordering.getName(), ordering.createField()));
        return namedFields;
    }

    private void forEachAccessor(MethodAnnotationConsumer consumer) {
        for (Method method : type.getDeclaredMethods()) {
            StructField annotation = method.getDeclaredAnnotation(StructField.class);
            if (annotation != null) {
                consumer.accept(method, annotation);
            }
        }
    }

    private String propertyName(Method method, StructField annotation) {
        String name = annotation.value();
        if ("".equals(name)) {
            String methodName = method.getName();
            if (methodName.startsWith("get") || methodName.startsWith("set")) {
                name = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            } else {
                throw new RuntimeException(
                        "Cannot determine struct property for accessor method: " + methodName);
            }
        }
        return name;
    }

    /**
     * Iterator interface for struct accessor methods.
     */
    @FunctionalInterface
    public interface MethodAnnotationConsumer {
        /**
         * Executed for each struct accessor method.
         *
         * @param method     Method instance.
         * @param annotation Struct field annotation.
         */
        void accept(Method method, StructField annotation);
    }
}
