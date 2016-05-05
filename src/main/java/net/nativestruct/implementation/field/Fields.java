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
import java.util.List;
import java.util.Map;

import net.bytebuddy.dynamic.DynamicType;
import net.nativestruct.AbstractStruct;
import net.nativestruct.StructField;

/**
 * Encapsulates struct fields.
 * It is build with the following sequence:
 * Name -> Accessor type -> Type -> Field counts -> Indexes -> Accessor
 */
public final class Fields {
    private Map<String, Field> fields;
    private FieldCounter counter = new FieldCounter();

    /**
     * Initializes instance with the list of accessor methods group by struct field names.
     *
     * @param type Map between field names and accessor methods.
     */
    public Fields(Class<?> type) {
        this.fields = buildFields(
                buildFieldOrdering(type,
                buildAccessorTypes(type)));
    }

    private Map<String, List<Accessor>> buildAccessorTypes(Class<?> type) {
        Map<String, List<Accessor>> result = new HashMap<>();
        forEachAccessor(type, (method, annotation) -> {
            List<Accessor> accessors = result.computeIfAbsent(
                    propertyName(method, annotation), name -> new ArrayList<>());
            Accessor accessor = AbstractAccessor.of(annotation.accessor(), method);
            if (!accessors.isEmpty()) {
                accessor.checkCompatibility(accessors.get(0));
            }
            accessors.add(accessor);
        });
        return result;
    }

    private List<FieldOrdering> buildFieldOrdering(
            Class<?> type, Map<String, List<Accessor>> accessorTypes) {

        Map<String, FieldOrdering> orderings = new HashMap<>();
        forEachAccessor(type, (method, annotation) -> {
            String fieldName = propertyName(method, annotation);
            FieldOrdering ordering = orderings.computeIfAbsent(fieldName,
                name -> {
                    List<Accessor> types = accessorTypes.get(name);
                    return new FieldOrdering(name, types.get(0).getType(), types);
                });
            if (!ordering.updateOrder(annotation.order())) {
                throw new AssertionError(String.format(
                    "Ambiguous field ordering: %s.%s", type.getSimpleName(), fieldName));
            }
        });

        List<FieldOrdering> orderingsList = new ArrayList<>(orderings.values());
        Collections.sort(orderingsList,
            (left, right) -> Integer.compare(left.getOrder(), right.getOrder()));
        return orderingsList;
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

    private Map<String, Field> buildFields(List<FieldOrdering> orderings) {
        Map<String, Field> namedFields = new HashMap<>();
        orderings.stream().forEach(
            ordering -> namedFields.put(ordering.getName(),
                ordering.createFieldWithIndex(counter)));
        return namedFields;
    }

    private void forEachAccessor(Class<?> type, MethodAnnotationConsumer consumer) {
        for (Method method : type.getDeclaredMethods()) {
            StructField annotation = method.getDeclaredAnnotation(StructField.class);
            if (annotation != null) {
                consumer.accept(method, annotation);
            }
        }
    }

    /**
     * Installs accessor methods implementation for struct fields.
     *
     * @param struct Class definition builder.
     * @return New class builder instance.
     */
    public DynamicType.Builder<AbstractStruct> installAccessors(
            DynamicType.Builder<AbstractStruct> struct) {
        DynamicType.Builder<AbstractStruct> result = struct;
        for (Field field : fields.values()) {
            result = field.installAccessors(result);
        }
        return result;
    }

    /**
     * @return The number of int fields.
     */
    public int intFields() {
        return counter.fieldsOf(int.class);
    }

    /**
     * @return The number of double fields.
     */
    public int doubleFields() {
        return counter.fieldsOf(double.class);
    }

    /**
     * @return The number of Object fields.
     */
    public int objectFields() {
        return counter.fieldsOf(Object.class);
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
