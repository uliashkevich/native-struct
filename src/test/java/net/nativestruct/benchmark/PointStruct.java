package net.nativestruct.benchmark;

import net.nativestruct.AbstractStruct;
import net.nativestruct.AccessorType;
import net.nativestruct.StructField;

public abstract class PointStruct extends AbstractStruct {
    @StructField
    public abstract double getX();
    @StructField
    public abstract double getY();
    @StructField(accessor = AccessorType.GETTER_INDEXED)
    public abstract double getX(int index);
    @StructField(accessor = AccessorType.GETTER_INDEXED)
    public abstract double getY(int index);

    @StructField
    public abstract void setX(double value);
    @StructField
    public abstract void setY(double value);

    public double square() {
        double x = getX();
        double y = getY();
        return x * x + y * y;
    }

    public double square(int index) {
        double x = getX(index);
        double y = getY(index);
        return x * x + y * y;
    }
}
