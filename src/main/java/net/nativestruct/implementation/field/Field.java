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

import java.util.List;

import net.bytebuddy.dynamic.DynamicType;
import net.nativestruct.AbstractStruct;

/**
* Encapsulated accessor methods of a struct field.
*/
public class Field {
    private int index;
    private FieldCounts counts;
    private List<Accessor> accessors;

    /**
     * Construct field object with field index and the counter object.
     * @param index Field index among the fields with the same type.
     * @param counts File counting object.
     * @param accessors Accessors list.
     */
    public Field(int index, FieldCounts counts, List<Accessor> accessors) {
        this.index = index;
        this.counts = counts;
        this.accessors = accessors;
    }

    /**
     * Builds bytecode for struct fields accessor methods.
     *
     * @param struct Accessor methods bytecode builder.
     * @return New instance of build.
     */
    public final DynamicType.Builder<AbstractStruct> installAccessors(
            DynamicType.Builder<AbstractStruct> struct) {

        DynamicType.Builder<AbstractStruct> result = struct;
        for (Accessor accessor : accessors) {
            result = accessor.install(result, index, counts);
        }
        return result;
    }
}
