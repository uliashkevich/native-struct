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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

/**
 * Bytecode generator of the int field getter.
 */
public final class GetterIndexedByteCodeAppender implements ByteCodeAppender {

    private int fields;
    private int index;
    private MethodDescription.InDefinedShape getter;
    private final TypeDescription.ForLoadedType returning;

    /**
     * Construct bytecode appender instance.
     * @param fields The number of integer fields in a struct.
     * @param index Index of the field being accessed.
     * @param getter Method description representing array accessor in the
*               {@link net.nativestruct.AbstractStruct} instance.
     * @param returning Return type description.
     */
    public GetterIndexedByteCodeAppender(int fields, int index,
                                         MethodDescription.InDefinedShape getter,
                                         TypeDescription.ForLoadedType returning) {
        this.fields = fields;
        this.index = index;
        this.getter = getter;
        this.returning = returning;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor,
                      Implementation.Context context,
                      MethodDescription method) {

        List<StackManipulation> commands = new ArrayList<>(Arrays.asList(
                MethodVariableAccess.REFERENCE.loadOffset(0),
                IntegerConstant.forValue(fields),
                MethodVariableAccess.INTEGER.loadOffset(1),
                IntegerConstant.forValue(index),
                MethodInvocation.invoke(getter)
        ));

        if (!returning.isPrimitive()) {
            commands.add(TypeCasting.to(returning));
        }

        commands.add(MethodReturn.returning(returning));

        StackManipulation.Size stackSize = new StackManipulation.Compound(commands)
                .apply(methodVisitor, context);

        return new Size(stackSize.getMaximalSize(), method.getStackSize());
    }
}
