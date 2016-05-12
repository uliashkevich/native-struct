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
package net.nativestruct.implementation.bytecode.constant;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.nativestruct.implementation.bytecode.AbstractImplementation;

/**
 * Bytecode generator of a child struct field getter.
 */
public class StructGetterByteCodeAppender implements ByteCodeAppender {
    private Class<?> type;
    private int index;

    /**
     * Construct bytecode appender instance.
     *
     * @param type Child struct type.
     * @param index Child struct field index within the child struct array.
     */
    public StructGetterByteCodeAppender(Class<?> type, int index) {
        this.type = type;
        this.index = index;
    }

    @Override
    public final Size apply(MethodVisitor methodVisitor,
                      Implementation.Context context,
                      MethodDescription method) {

        TypeDescription returning = new TypeDescription.ForLoadedType(type);

        StackManipulation.Size stackSize = new StackManipulation.Compound(
                MethodVariableAccess.REFERENCE.loadOffset(0),
                IntegerConstant.forValue(index),
                MethodInvocation.invoke(AbstractImplementation.STRUCT_COMPOSITE),
                TypeCasting.to(returning),
                MethodReturn.returning(returning)
        ).apply(methodVisitor, context);

        return new Size(stackSize.getMaximalSize(), method.getStackSize());
    }
}
