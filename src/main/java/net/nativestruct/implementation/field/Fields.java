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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.nativestruct.AbstractStruct;

/**
 * Encapsulates struct fields.
 * It is build with the following sequence:
 * Name -> Accessor type -> Type -> Field counts -> Indexes -> Accessor
 */
public final class Fields implements FieldLike {
    private final Class<?> type;
    private final int index;
    private Map<String, FieldLike> fields;
    private FieldCounter counter;
    private List<Accessor> accessors;
    private List<Fields> composites;

    /**
     * Initializes instance with the list of accessor methods group by struct field names.
     *
     * @param type Map between field names and accessor methods.
     * @param index Composite property index inside composites array.
     * @param counter Field counter object.
     * @param accessors The list of the field accessor methods.
     * @param fields Child fields list.
     */
    Fields(Class<?> type, int index, FieldCounter counter, List<Accessor> accessors,
           Map<String, FieldLike> fields) {
        this.type = type;
        this.index = index;
        this.counter = counter;
        this.accessors = accessors;

        this.fields = fields;
        this.composites = fields.values().stream()
                .filter(FieldLike::isComposite)
                .map(field -> (Fields) field)
                .collect(Collectors.toList());

        if (!type.isInterface() && !AbstractStruct.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Invalid accessor base type: " + type);
        }
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public DynamicType.Builder<AbstractStruct> installAccessors(
            DynamicType.Builder<AbstractStruct> struct) {

        DynamicType.Builder<AbstractStruct> result = struct;
        for (Accessor accessor : accessors) {
            result = accessor.install(result, index, counter);
        }
        return result;
    }

    /**
     * Builds and returns a list of all accessors, including child struct fields and their children.
     *
     * @return A list of accessors.
     */
    public List<AbstractStruct> buildAccessors() {
        List<AbstractStruct> result = new ArrayList<>();

        result.add(buildAccessorInstance(bareStruct -> {
            DynamicType.Builder<AbstractStruct> struct = bareStruct;
            for (FieldLike field : fields.values()) {
                struct = field.installAccessors(struct);
            }
            return struct;
        }));

        for (Fields composite : composites) {
            result.addAll(composite.buildAccessors());
        }

        return result;
    }

    private AbstractStruct buildAccessorInstance(
            Function<DynamicType.Builder<AbstractStruct>,
                    DynamicType.Builder<AbstractStruct>> install) {
        try {
            DynamicType.Builder<AbstractStruct> bareStruct =
                    AbstractStruct.class.isAssignableFrom(this.type)
                            ? new ByteBuddy().subclass((Class<AbstractStruct>) this.type)
                            : new ByteBuddy().subclass(AbstractStruct.class).implement(this.type);

            return install.apply(bareStruct)
                    .make()
                    .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot create accessor for " + this.type, e);
        }
    }

    /**
     * @return The number of child struct fields.
     */
    public int composites() {
        return counter.composites();
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
     * @param field Field instance.
     * @return Whether the given field belongs to the field list.
     */
    public boolean hasField(Field field) {
        return fields.containsValue(field);
    }

    /**
     * @param name Field name.
     * @return Field instance by name.
     */
    public FieldLike field(String name) {
        return fields.get(name);
    }

    /**
     * @return The list of the struct field names.
     */
    public List<String> allFieldNames() {
        return new ArrayList<>(fields.keySet());
    }
}
