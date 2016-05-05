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
package net.nativestruct.implementation.bytecode.direct;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.nativestruct.implementation.bytecode.AbstractImplementation;

/**
 * Implementation of the int field setter.
 */
public final class SetterDirectImplementation extends AbstractImplementation {
    /**
     * Construct bytecode appender instance.
     *
     * @param type Field type.
     * @param fields The number of integer fields in a struct.
     * @param index Index of the field being updated.
     */
    public SetterDirectImplementation(Class<?> type, int fields, int index) {
        super(type, fields, index);
    }

    @Override
    public ByteCodeAppender appender(Target target) {
        Class<?> setterType = type().isPrimitive() ? type() : Object.class;
        MethodDescription.InDefinedShape setter = FIELD_SETTERS.get(setterType);
        if (setter == null) {
            throw new AssertionError("No setter for type: " + type());
        }
        return new SetterDirectByteCodeAppender(fields(), index(), STRUCT_CURRENT, setter,
                MethodVariableAccess.of(new TypeDescription.ForLoadedType(type())));
    }
}
