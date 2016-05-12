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
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.nativestruct.AbstractStruct;
import net.nativestruct.AccessorType;
import net.nativestruct.implementation.bytecode.constant.StructGetterByteCodeAppender;
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
     * Construct base accessor instance.
     *
     * @param type Field type.
     * @param name Field property name.
     */
    protected AbstractAccessor(Class<?> type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Creates {@link AbstractAccessor} instance based on accessor type.
     *
     * @param accessorType Accessor type - getter or setter, indexed or non-indexed.
     * @param method       Accessor method declaration.
     * @return Accessor type instance.
     */
    public static Accessor of(AccessorType accessorType, Method method) {
        Accessor result;
        if (AbstractStruct.class.isAssignableFrom(method.getReturnType())) {
            if (!accessorType.isGetter(method.getName())) {
                throw new AssertionError("Unsupported composite accessor: " + accessorType);
            }
            result = new GetterComposite(method);
        } else {
            String name = method.getName();
            if (accessorType.isGetter(name)) {
                result = new GetterDirect(method);
            } else if (accessorType.isSetter(name)) {
                result = new SetterDirect(method);
            } else if (accessorType == AccessorType.GETTER_INDEXED) {
                result = new GetterIndexed(method);
            } else if (accessorType == AccessorType.SETTER_INDEXED) {
                result = new SetterIndexed(method);
            } else {
                throw new AssertionError(String.format(
                        "Unsupported accessor type %s for method %s", accessorType, name));
            }
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
     *  @param parameterType Accessor type.
     *
     */
    protected final void checkSupportedType(Class<?> parameterType) {
        if (parameterType.isPrimitive() && !SUPPORTED_TYPES.contains(parameterType)) {
            throw new AssertionError("Unsupported primitive type: " + parameterType);
        }
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
     * @return Method parameter.
     */
    protected static Method checkParameterCount(Method method, int paramCount) {
        if (method.getParameterCount() != paramCount) {
            throw new AssertionError(String.format(
                    "Accessor method %s should have %d parameters", method, paramCount));
        }
        return method;
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
            super(method.getReturnType(), method.getName());
            checkParameterCount(method, 1);
            checkIndexParameter(method);
            checkSupportedType(method.getReturnType());
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
            super(checkParameterCount(method, 2).getParameterTypes()[1], method.getName());
            checkIndexParameter(method);
            checkSupportedType(method.getParameterTypes()[1]);
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
            super(method.getReturnType(), method.getName());
            checkParameterCount(method, 0);
            checkSupportedType(method.getReturnType());
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
            super(checkParameterCount(method, 1).getParameterTypes()[0], method.getName());
            checkSupportedType(method.getParameterTypes()[0]);
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

    /**
     * Represents child struct accessor method.
     */
    public static final class GetterComposite extends AbstractAccessor {
        /**
         * Constructs getter.
         *
         * @param method Accessor interface method.
         */
        public GetterComposite(Method method) {
            super(method.getReturnType(), method.getName());
            checkParameterCount(method, 0);
        }

        @Override
        public DynamicType.Builder<AbstractStruct> install(
                DynamicType.Builder<AbstractStruct> struct, int index, FieldCounts counts) {
            return struct
                    .defineMethod(getName(), getType(), Visibility.PUBLIC)
                    .withParameters(Arrays.<Type>asList())
                    .intercept(new Implementation() {
                        @Override
                        public ByteCodeAppender appender(Target target) {
                            return new StructGetterByteCodeAppender(getType(), index);
                        }

                        @Override
                        public InstrumentedType prepare(InstrumentedType type) {
                            return type;
                        }
                    });
        }
    }
}
