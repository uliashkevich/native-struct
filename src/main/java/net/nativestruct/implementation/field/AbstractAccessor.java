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
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.nativestruct.AbstractStruct;
import net.nativestruct.AccessorType;
import net.nativestruct.implementation.bytecode.direct.GetterDirectImplementation;
import net.nativestruct.implementation.bytecode.direct.SetterDirectImplementation;
import net.nativestruct.implementation.bytecode.indexed.GetterIndexedImplementation;
import net.nativestruct.implementation.bytecode.indexed.SetterIndexedImplementation;

/**
 * Encapsulates accessor type (getter or setter) and field type.
 */
public abstract class AbstractAccessor implements Accessor {
    private static final List<Class<?>> SUPPORTED_TYPES = Arrays.asList(int.class, double.class);

    private Class<?> type;
    private String name;

    /**
     * Creates {@link AbstractAccessor} instance based on accessor type.
     *
     * @param accessorType Accessor type - getter or setter, indexed or non-indexed.
     * @param method       Accessor method declaration.
     * @return Accessor type instance.
     */
    public static Accessor of(AccessorType accessorType, Method method) {
        Accessor result;
        if (accessorType == AccessorType.GETTER_INDEXED) {
            result = new GetterIndexed(method);
        } else if (accessorType == AccessorType.SETTER_INDEXED) {
            result = new SetterIndexed(method);
        } else if (accessorType == AccessorType.AUTO && method.getName().startsWith("get")) {
            result = new GetterDirect(method);
        } else if (accessorType == AccessorType.AUTO && method.getName().startsWith("set")) {
            result = new SetterDirect(method);
        } else {
            throw new AssertionError(String.format(
                    "Unsupported accessor type %s for method %s", accessorType, method.getName()));
        }
        return result;
    }

    @Override
    public final Class<?> getType() {
        return type;
    }

    /**
     * @return Accessor method declaration.
     */
    protected final String getName() {
        return name;
    }

    @Override
    public final void checkCompatibility(Accessor accessor) {
        if (getType() != accessor.getType()) {
            throw new IllegalStateException(String.format(
                    "Ambiguous accessor types: %s & %s", getType(), accessor.getType()));
        }
    }

    /**
     * Checks that the accessor type is supported.
     *
     * @param parameterType Accessor type.
     * @param method Accessor method declaration.
     */
    protected final void checkTypeAndAssign(Class<?> parameterType, Method method) {
        if (parameterType.isPrimitive() && !SUPPORTED_TYPES.contains(parameterType)) {
            throw new AssertionError("Unsupported primitive type: " + parameterType);
        }
        this.type = parameterType;
        this.name = method.getName();
    }

    /**
     * Checks that accessor has index parameter.
     *  @param method     Accessor method declaration.
     *
     */
    protected final void checkIndexParameter(Method method) {
        if (method.getParameterTypes()[0] != int.class) {
            throw new AssertionError(String.format(
                    "Accessor method %s should have first integer parameter", method));
        }
    }

    /**
     * Checks that access has an expected parameter count.
     *
     * @param method     Accessor method declaration.
     * @param paramCount Expected parameter count.
     */
    protected final void checkParameterCount(Method method, int paramCount) {
        if (method.getParameterCount() != paramCount) {
            throw new AssertionError(String.format(
                    "Accessor method %s should have %d parameters", method, paramCount));
        }
    }

    /**
     * Represents getter method of an accessor interface.
     */
    public static final class GetterIndexed extends AbstractAccessor {
        /**
         * Constructs getter.
         *
         * @param method Accessor interface method.
         */
        public GetterIndexed(Method method) {
            checkParameterCount(method, 1);
            checkIndexParameter(method);
            checkTypeAndAssign(method.getReturnType(), method);
        }

        @Override
        public DynamicType.Builder<AbstractStruct> install(
                DynamicType.Builder<AbstractStruct> struct, int index, FieldCounts counts) {
            return struct
                    .defineMethod(getName(), getType(), Visibility.PUBLIC)
                    .withParameters(int.class)
                    .intercept(new GetterIndexedImplementation(
                            getType(), counts.fieldsOf(getType()), index));
        }
    }

    /**
     * Represents setter method of an accessor interface.
     */
    public static final class SetterIndexed extends AbstractAccessor {
        /**
         * Constructs setter.
         *
         * @param method Accessor interface method.
         */
        public SetterIndexed(Method method) {
            checkParameterCount(method, 2);
            checkIndexParameter(method);
            checkTypeAndAssign(method.getParameterTypes()[1], method);
        }

        @Override
        public DynamicType.Builder<AbstractStruct> install(
                DynamicType.Builder<AbstractStruct> struct, int index, FieldCounts counts) {
            return struct
                    .defineMethod(getName(), void.class, Visibility.PUBLIC)
                    .withParameters(int.class, getType())
                    .intercept(new SetterIndexedImplementation(
                            getType(), counts.fieldsOf(getType()), index));
        }
    }

    /**
     * Represents getter method of an accessor interface.
     */
    public static final class GetterDirect extends AbstractAccessor {
        /**
         * Constructs getter.
         *
         * @param method Accessor interface method.
         */
        public GetterDirect(Method method) {
            checkParameterCount(method, 0);
            checkTypeAndAssign(method.getReturnType(), method);
        }

        @Override
        public DynamicType.Builder<AbstractStruct> install(
                DynamicType.Builder<AbstractStruct> struct, int index, FieldCounts counts) {
            return struct
                    .defineMethod(getName(), getType(), Visibility.PUBLIC)
                    .withParameters(Arrays.<Type>asList())
                    .intercept(new GetterDirectImplementation(
                            getType(), counts.fieldsOf(getType()), index));
        }
    }

    /**
     * Represents setter method of an accessor interface.
     */
    public static final class SetterDirect extends AbstractAccessor {
        /**
         * Constructs setter.
         *
         * @param method Accessor interface method.
         */
        public SetterDirect(Method method) {
            checkParameterCount(method, 1);
            checkTypeAndAssign(method.getParameterTypes()[0], method);
        }

        @Override
        public DynamicType.Builder<AbstractStruct> install(
                DynamicType.Builder<AbstractStruct> struct, int index, FieldCounts counts) {
            return struct
                    .defineMethod(getName(), void.class, Visibility.PUBLIC)
                    .withParameters(getType())
                    .intercept(new SetterDirectImplementation(
                            getType(), counts.fieldsOf(getType()), index));
        }
    }
}
