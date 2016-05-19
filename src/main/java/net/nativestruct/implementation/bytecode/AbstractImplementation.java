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
package net.nativestruct.implementation.bytecode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.nativestruct.AbstractStruct;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;

/**
 * Base class for struct getter/setter bytecode implementation.
 */
public abstract class AbstractImplementation implements Implementation {
    public static final TypeDescription.ForLoadedType STRUCT_TYPE
            = new TypeDescription.ForLoadedType(AbstractStruct.class);
    public static final MethodList<MethodDescription.InDefinedShape> STRUCT_METHODS
            = STRUCT_TYPE.getDeclaredMethods();

    public static final MethodDescription.InDefinedShape INT_FIELD_GETTER
            = STRUCT_METHODS.filter(named("intFieldIndexed")).getOnly();
    public static final MethodDescription.InDefinedShape INT_FIELD_SETTER
            = STRUCT_METHODS.filter(named("updateIntFieldIndexed")).getOnly();

    public static final MethodDescription.InDefinedShape DOUBLE_FIELD_GETTER
            = STRUCT_METHODS.filter(named("doubleFieldIndexed")).getOnly();
    public static final MethodDescription.InDefinedShape DOUBLE_FIELD_SETTER
            = STRUCT_METHODS.filter(named("updateDoubleFieldIndexed")).getOnly();

    public static final MethodDescription.InDefinedShape OBJECT_FIELD_GETTER
            = STRUCT_METHODS.filter(named("objectFieldIndexed")).getOnly();
    public static final MethodDescription.InDefinedShape OBJECT_FIELD_SETTER
            = STRUCT_METHODS.filter(named("updateObjectFieldIndexed")).getOnly();

    public static final MethodDescription.InDefinedShape STRUCT_CURRENT
            = STRUCT_METHODS.filter(named("current").and(returns(int.class))).getOnly();

    public static final MethodDescription.InDefinedShape STRUCT_COMPOSITE
            = STRUCT_METHODS.filter(named("composite")).getOnly();

    public static final Map<Class<?>, MethodDescription.InDefinedShape> FIELD_GETTERS;
    public static final Map<Class<?>, MethodDescription.InDefinedShape> FIELD_SETTERS;

    static {
        Map<Class<?>, MethodDescription.InDefinedShape> map = new HashMap<>();
        map.put(int.class, INT_FIELD_GETTER);
        map.put(double.class, DOUBLE_FIELD_GETTER);
        map.put(Object.class, OBJECT_FIELD_GETTER);
        FIELD_GETTERS = Collections.unmodifiableMap(map);

        map = new HashMap<>();
        map.put(int.class, INT_FIELD_SETTER);
        map.put(double.class, DOUBLE_FIELD_SETTER);
        map.put(Object.class, OBJECT_FIELD_SETTER);
        FIELD_SETTERS = Collections.unmodifiableMap(map);
    }

    private Class<?> type;
    private int fields;
    private int index;

    /**
     * Construct bytecode appender instance.
     *
     * @param type Field type.
     * @param fields The number of integer fields in a struct.
     * @param index Index of the field being updated.
     */
    public AbstractImplementation(Class<?> type, int fields, int index) {
        this.type = type;
        this.fields = fields;
        this.index = index;
    }

    @Override
    public final InstrumentedType prepare(InstrumentedType instrumented) {
        return instrumented;
    }

    /**
     * @return Field type for which accessor method is generated.
     */
    public final Class<?> type() {
        return type;
    }

    /**
     * @return The number of fields of the same type.
     */
    public final int fields() {
        return fields;
    }

    /**
     * @return Field index within the fields with the same type.
     */
    public final int index() {
        return index;
    }
}
