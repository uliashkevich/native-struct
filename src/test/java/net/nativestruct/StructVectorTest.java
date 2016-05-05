package net.nativestruct;

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
}
