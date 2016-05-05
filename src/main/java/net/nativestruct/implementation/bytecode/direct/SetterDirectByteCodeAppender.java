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
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;

/**
 * Bytecode generator of the int field setter.
 */
public final class SetterDirectByteCodeAppender implements ByteCodeAppender {

    private int fields;
    private int index;
    private MethodDescription.InDefinedShape current;
    private MethodDescription.InDefinedShape setter;
    private MethodVariableAccess access;

    /**
     * Construct bytecode appender instance.
     *
     * @param fields The number of integer fields in a struct.
     * @param index  Index of the field being updated.
     * @param current Descriptor of field `current`.
     * @param setter Method description representing array accessor in the
     *               {@link net.nativestruct.AbstractStruct} instance.
     * @param access Method variable access.
     */
    public SetterDirectByteCodeAppender(int fields, int index,
                                        MethodDescription.InDefinedShape current,
                                        MethodDescription.InDefinedShape setter,
                                        MethodVariableAccess access) {
        this.fields = fields;
        this.index = index;
        this.current = current;
        this.setter = setter;
        this.access = access;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor,
                      Implementation.Context context,
                      MethodDescription method) {

        StackManipulation.Size stackSize = new StackManipulation.Compound(
                MethodVariableAccess.REFERENCE.loadOffset(0),
                IntegerConstant.forValue(fields),
                MethodVariableAccess.REFERENCE.loadOffset(0),
                MethodInvocation.invoke(current),
                IntegerConstant.forValue(index),
                access.loadOffset(1),
                MethodInvocation.invoke(setter),
                MethodReturn.VOID
        ).apply(methodVisitor, context);

        return new Size(stackSize.getMaximalSize(), method.getStackSize());
    }
}
