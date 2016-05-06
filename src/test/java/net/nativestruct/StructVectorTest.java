package net.nativestruct;

import java.util.Arrays;
import java.util.stream.Collectors;

import net.nativestruct.implementation.field.Field;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class StructVectorTest {
    @Test
    public void indexedAccessorsTest() throws Exception {
        StructVector<StructIntOnly> struct = new StructVector<>(StructIntOnly.class, 10);
        struct.resize(2);
        StructIntOnly accessor = struct.accessor();
        accessor.updateIntField(1, 55);
        assertEquals(0, accessor.getIntField(0));
        assertEquals(55, accessor.getIntField(1));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void indexedAccessorBoundsError1Test() throws Exception {
        StructVector<StructIntOnly> struct = new StructVector<>(StructIntOnly.class, 10);
        struct.resize(2);
        StructIntOnly accessor = struct.accessor();
        accessor.updateIntField(-1, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void indexedAccessorBoundsError2Test() throws Exception {
        StructVector<StructIntOnly> struct = new StructVector<>(StructIntOnly.class, 10);
        struct.resize(2);
        StructIntOnly accessor = struct.accessor();
        accessor.updateIntField(10, 0);
    }

    public interface StructIntOnly {
        @StructField(value = "int", accessor = AccessorType.GETTER_INDEXED)
        int getIntField(int record);

        @StructField(value = "int", accessor = AccessorType.SETTER_INDEXED)
        void updateIntField(int record, int value);
    }

    @Test
    public void indexedAccessorsTwoFieldsTest() throws Exception {
        StructVector<StructTwoInts> struct = new StructVector<>(StructTwoInts.class, 2);
        struct.resize(2);
        StructTwoInts accessor = struct.accessor();

        accessor.updateIntField(0, 11);
        accessor.updateInt2Field(0, 22);
        accessor.updateIntField(1, 33);
        accessor.updateInt2Field(1, 44);

        assertEquals(11, accessor.getIntField(0));
        assertEquals(22, accessor.getInt2Field(0));
        assertEquals(33, accessor.getIntField(1));
        assertEquals(44, accessor.getInt2Field(1));
    }

    public interface StructTwoInts {
        @StructField(value = "int", accessor = AccessorType.GETTER_INDEXED)
        int getIntField(int record);

        @StructField(value = "int", accessor = AccessorType.SETTER_INDEXED)
        void updateIntField(int record, int value);

        @StructField(value = "int2", accessor = AccessorType.GETTER_INDEXED)
        int getInt2Field(int record);

        @StructField(value = "int2", accessor = AccessorType.SETTER_INDEXED)
        void updateInt2Field(int record, int value);
    }

    @Test
    public void fieldOrderingTest() throws Exception {
        StructVector<StructTwoIntsOrdering> struct = new StructVector<>(StructTwoIntsOrdering.class, 1);
        struct.resize(1);
        StructTwoIntsOrdering accessor = struct.accessor();

        accessor.updateIntField(0, 11);
        accessor.updateInt2Field(0, 22);

        assertArrayEquals(new int[]{22, 11}, struct.integers());
    }

    public interface StructTwoIntsOrdering {
        @StructField(value = "int", order = 2, accessor = AccessorType.SETTER_INDEXED)
        void updateIntField(int record, int value);

        @StructField(value = "int2", order = 1, accessor = AccessorType.SETTER_INDEXED)
        void updateInt2Field(int record, int value);
    }

    @Test
    public void fieldIntTwoDoubles() throws Exception {
        StructVector<StructIntTwoDoubles> struct = new StructVector<>(StructIntTwoDoubles.class, 1);
        struct.resize(1);
        StructIntTwoDoubles accessor = struct.accessor();

        accessor.updateIntField(0, 11);
        accessor.updateDoubleField(0, 1.5);
        accessor.updateDouble2Field(0, 2.5);

        assertArrayEquals(new int[]{11}, struct.integers());
        assertArrayEquals(new double[]{2.5, 1.5}, struct.doubles(), 0.0);
    }

    public interface StructIntTwoDoubles {
        @StructField(value = "int", accessor = AccessorType.SETTER_INDEXED)
        void updateIntField(int record, int value);

        @StructField(value = "double", accessor = AccessorType.SETTER_INDEXED)
        void updateDoubleField(int record, double value);

        @StructField(value = "double2", accessor = AccessorType.SETTER_INDEXED)
        void updateDouble2Field(int record, double value);
    }

    @Test
    public void objectIndexedAccessorsTest() throws Exception {
        StructVector<StructObjectOnly> struct = new StructVector<>(StructObjectOnly.class, 10);
        struct.resize(2);
        StructObjectOnly accessor = struct.accessor();
        accessor.updateObjectField(1, "55");
        accessor.updateStringField(0, "22");
        assertEquals(null, accessor.getObjectField(0));
        assertEquals("55", accessor.getObjectField(1));
        assertEquals("22", accessor.getStringField(0));
        assertEquals(null, accessor.getStringField(1));
    }

    public interface StructObjectOnly {
        @StructField(value = "Object", accessor = AccessorType.GETTER_INDEXED)
        Object getObjectField(int record);

        @StructField(value = "Object", accessor = AccessorType.SETTER_INDEXED)
        void updateObjectField(int record, Object value);

        @StructField(value = "String", accessor = AccessorType.GETTER_INDEXED)
        String getStringField(int record);

        @StructField(value = "String", accessor = AccessorType.SETTER_INDEXED)
        void updateStringField(int record, String value);
    }

    @Test
    public void accessorDirectTest() {
        StructVector<StructDirect> struct = new StructVector<>(StructDirect.class, 2);
        struct.resize(2);
        StructDirect accessor = struct.accessor();
        accessor.current(1);
        accessor.setInt(55);
        assertArrayEquals(new int[]{0, 55}, struct.integers());

        accessor.current(0);
        accessor.setDouble(1.5);
        assertArrayEquals(new double[]{1.5, 0.0}, struct.doubles(), 1e-6);
    }

    public static abstract class StructDirect extends AbstractStruct {
        @StructField
        public abstract int getInt();
        @StructField
        public abstract void setInt(int value);

        @StructField
        public abstract double getDouble();
        @StructField
        public abstract void setDouble(double value);

        @StructField
        public abstract String getString();
        @StructField
        public abstract void setString(String value);
    }

    public static abstract class StructDirectIntOnly extends AbstractStruct {
        @StructField
        public abstract int getInt();
        @StructField
        public abstract void setInt(int value);
    }

    public static abstract class StructDirectDoubleOnly extends AbstractStruct {
        @StructField
        public abstract double getDouble();
        @StructField
        public abstract void setDouble(double value);
    }

    public static abstract class StructDirectStringOnly extends AbstractStruct {
        @StructField
        public abstract String getString();
        @StructField
        public abstract void setString(String value);
    }

    @Test
    public void insertTest() {
        StructVector<StructDirect> struct = new StructVector<>(StructDirect.class, 2);
        struct.resize(2);
        StructDirect accessor = struct.accessor();
        accessor.current(0);
        accessor.setInt(1);
        accessor.setDouble(1.5);
        accessor.setString("11");
        accessor.current(1);
        accessor.setInt(2);
        accessor.setDouble(2.5);
        accessor.setString("22");

        assertEquals(1, accessor.current());

        struct.insert(1, 2);
        assertEquals(-1, accessor.current());

        assertArrayEquals(new int[]{1, 0, 0, 2}, struct.integers());
        assertArrayEquals(new double[]{1.5, 0.0, 0.0, 2.5}, struct.doubles(), 1e-6);
        assertArrayEquals(new Object[]{"11", null, null, "22"}, struct.objects());
    }

    @Test
    public void insertLastTest() {
        StructVector<StructDirect> struct = new StructVector<>(StructDirect.class, 2);
        struct.resize(2);
        StructDirect accessor = struct.accessor();
        accessor.current(0);
        accessor.setInt(1);
        accessor.setDouble(1.5);
        accessor.setString("11");
        accessor.current(1);
        accessor.setInt(2);
        accessor.setDouble(2.5);
        accessor.setString("22");

        assertEquals(1, accessor.current());

        int index = struct.insertLast(2);
        assertEquals(2, index);
        assertEquals(-1, accessor.current());
        accessor.current(index + 1);
        accessor.setInt(3);
        accessor.setDouble(3.5);
        accessor.setString("33");

        assertArrayEquals(new int[]{1, 2, 0, 3}, struct.integers());
        assertArrayEquals(new double[]{1.5, 2.5, 0.0, 3.5}, struct.doubles(), 1e-6);
        assertArrayEquals(new Object[]{"11", "22", null, "33"}, struct.objects());
    }

    @Test
    public void binarySearchIntTest() {
        StructVector<StructDirectIntOnly> struct = new StructVector<>(StructDirectIntOnly.class, 100);
        struct.resize(4);
        StructDirectIntOnly accessor = struct.accessor();

        accessor.current(0);
        accessor.setInt(30);
        accessor.current(1);
        accessor.setInt(40);
        accessor.current(2);
        accessor.setInt(50);
        accessor.current(3);
        accessor.setInt(60);

        Field intField = struct.field("int");
        assertEquals(-1, struct.binarySearch(intField, 25));
        assertEquals(0, struct.binarySearch(intField, 30));
        assertEquals(-2, struct.binarySearch(intField, 35));
        assertEquals(1, struct.binarySearch(intField, 40));
        assertEquals(-3, struct.binarySearch(intField, 45));
        assertEquals(2, struct.binarySearch(intField, 50));
        assertEquals(-4, struct.binarySearch(intField, 55));
        assertEquals(3, struct.binarySearch(intField, 60));
        assertEquals(-5, struct.binarySearch(intField, 65));
    }

    @Test
    public void binarySearchDoubleTest() {
        StructVector<StructDirectDoubleOnly> struct = new StructVector<>(StructDirectDoubleOnly.class, 100);
        struct.resize(4);
        StructDirectDoubleOnly accessor = struct.accessor();

        accessor.current(0);
        accessor.setDouble(3.5);
        accessor.current(1);
        accessor.setDouble(4.5);
        accessor.current(2);
        accessor.setDouble(5.5);
        accessor.current(3);
        accessor.setDouble(6.5);

        Field doubleField = struct.field("double");
        assertEquals(-1, struct.binarySearch(doubleField, 3.0));
        assertEquals(0, struct.binarySearch(doubleField, 3.5));
        assertEquals(-2, struct.binarySearch(doubleField, 4.0));
        assertEquals(1, struct.binarySearch(doubleField, 4.5));
        assertEquals(-3, struct.binarySearch(doubleField, 5.0));
        assertEquals(2, struct.binarySearch(doubleField, 5.5));
        assertEquals(-4, struct.binarySearch(doubleField, 6.0));
        assertEquals(3, struct.binarySearch(doubleField, 6.5));
        assertEquals(-5, struct.binarySearch(doubleField, 7.0));
    }

    @Test
    public void binarySearchObjectTest() {
        StructVector<StructDirectStringOnly> struct = new StructVector<>(StructDirectStringOnly.class, 100);
        struct.resize(4);
        StructDirectStringOnly accessor = struct.accessor();

        accessor.current(0);
        accessor.setString("30");
        accessor.current(1);
        accessor.setString("40");
        accessor.current(2);
        accessor.setString("50");
        accessor.current(3);
        accessor.setString("60");

        Field stringField = struct.field("string");
        assertEquals(-1, struct.binarySearch(stringField, "25"));
        assertEquals(0, struct.binarySearch(stringField, "30"));
        assertEquals(-2, struct.binarySearch(stringField, "35"));
        assertEquals(1, struct.binarySearch(stringField, "40"));
        assertEquals(-3, struct.binarySearch(stringField, "45"));
        assertEquals(2, struct.binarySearch(stringField, "50"));
        assertEquals(-4, struct.binarySearch(stringField, "55"));
        assertEquals(3, struct.binarySearch(stringField, "60"));
        assertEquals(-5, struct.binarySearch(stringField, "65"));
    }

    @Test
    public void sortTest() {
        StructVector<StructDirect> struct = new StructVector<>(StructDirect.class, 8);
        struct.resize(7);
        StructDirect accessor = struct.accessor();

        int index = -1;
        updateIntegerAndString(accessor, ++index, 20, "33");
        updateIntegerAndString(accessor, ++index, 10, "44");
        updateIntegerAndString(accessor, ++index, 60, "55");
        updateIntegerAndString(accessor, ++index, 40, "66");
        updateIntegerAndString(accessor, ++index, 30, "77");
        updateIntegerAndString(accessor, ++index, 70, "88");
        updateIntegerAndString(accessor, ++index, 50, "99");

        struct.sort(struct.field("int"));

        assertArrayEquals(new int[]{10, 20, 30, 40, 50, 60, 70, 0}, struct.integers());
        assertArrayEquals(new Object[]{"44", "33", "77", "66", "99", "55", "88", null}, struct.objects());

        struct.sort(struct.field("string"));

        assertArrayEquals(new int[]{20, 10, 60, 40, 30, 70, 50, 0}, struct.integers());
        assertArrayEquals(new Object[]{"33", "44", "55", "66", "77", "88", "99", null}, struct.objects());
    }

    @Test
    public void sortReverseTest() {
        StructVector<StructDirect> struct = new StructVector<>(StructDirect.class, 8);
        struct.resize(7);
        StructDirect accessor = struct.accessor();

        int index = -1;
        updateIntegerAndString(accessor, ++index, 20, "33");
        updateIntegerAndString(accessor, ++index, 10, "44");
        updateIntegerAndString(accessor, ++index, 60, "55");
        updateIntegerAndString(accessor, ++index, 40, "66");
        updateIntegerAndString(accessor, ++index, 30, "77");
        updateIntegerAndString(accessor, ++index, 70, "88");
        updateIntegerAndString(accessor, ++index, 50, "99");

        struct.sort(struct.field("string"), (String left, String right) -> right.compareTo(left));

        assertEquals(Arrays.asList(50, 70, 30, 40, 60, 10, 20, 0),
                Arrays.stream(struct.integers()).boxed().collect(Collectors.toList()));
        assertEquals(Arrays.asList("99", "88", "77", "66", "55", "44", "33", null),
                Arrays.stream(struct.objects()).collect(Collectors.toList()));
    }

    @Test
    public void stableSortTest() {
        StructVector<StructDirect> struct = new StructVector<>(StructDirect.class, 16);
        struct.resize(14);
        StructDirect accessor = struct.accessor();

        int index = -1;
        updateIntegerAndString(accessor, ++index, 20, "33");
        updateIntegerAndString(accessor, ++index, 10, "44");
        updateIntegerAndString(accessor, ++index, 60, "55");
        updateIntegerAndString(accessor, ++index, 40, "66");
        updateIntegerAndString(accessor, ++index, 30, "77");
        updateIntegerAndString(accessor, ++index, 70, "88");
        updateIntegerAndString(accessor, ++index, 50, "99");

        updateIntegerAndString(accessor, ++index, 20, "331");
        updateIntegerAndString(accessor, ++index, 10, "441");
        updateIntegerAndString(accessor, ++index, 60, "551");
        updateIntegerAndString(accessor, ++index, 40, "661");
        updateIntegerAndString(accessor, ++index, 30, "771");
        updateIntegerAndString(accessor, ++index, 70, "881");
        updateIntegerAndString(accessor, ++index, 50, "991");

        struct.sort(struct.field("int"));

        assertEquals(Arrays.asList(
                        10, 10, 20, 20, 30, 30, 40, 40, 50, 50, 60, 60, 70, 70, 0, 0),
                Arrays.stream(struct.integers()).boxed().collect(Collectors.toList()));
        assertEquals(Arrays.asList(
                        "44", "441", "33", "331", "77", "771", "66", "661",
                        "99", "991", "55", "551", "88", "881", null, null),
                Arrays.stream(struct.objects()).collect(Collectors.toList()));
    }

    private void updateIntegerAndString(StructDirect accessor, int n, int integer, String string) {
        accessor.current(n);
        accessor.setInt(integer);
        accessor.setString(string);
    }
}
