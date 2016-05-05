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
package net.nativestruct.implementation.bytecode.indexed;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.nativestruct.implementation.bytecode.AbstractImplementation;

/**
 * Implementation of int field getter.
 */
public final class GetterIndexedImplementation extends AbstractImplementation {
    /**
     * Construct getter method implementation for retrieving field value from primitive array.
     * @param type Primitive type.
     * @param fields The number of struct fields of the same primitive type.
     * @param index Index of the field.
     */
    public GetterIndexedImplementation(Class<?> type, int fields, int index) {
        super(type, fields, index);
    }

    @Override
    public ByteCodeAppender appender(Target target) {
        Class<?> getterType = type().isPrimitive() ? type() : Object.class;
        MethodDescription.InDefinedShape getter = FIELD_GETTERS.get(getterType);
        if (getter == null) {
            throw new AssertionError("No getter for type: " + type());
        }
        return new GetterIndexedByteCodeAppender(
                fields(), index(), getter, new TypeDescription.ForLoadedType(type()));
    }
}
