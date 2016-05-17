package net.nativestruct.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.nativestruct.StructVector;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarks.
 */
@State(Scope.Benchmark)
public class Benchmarks {
    private static final int SIZE = 1_000_000;
    private static final Random random = new Random(System.currentTimeMillis());

    private List<Point> pointsList;
    private StructVector<PointStruct> pointsVector;
    private PointStruct pointsAccessor;

    public double publishResult;

    @Setup(Level.Trial)
    public void setUp() {
        pointsList = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            pointsList.add(new Point(random.nextDouble(), random.nextDouble()));
        }

        pointsVector = new StructVector<>(PointStruct.class, SIZE);
        pointsAccessor = pointsVector.accessor();
        for (int i = 0; i < SIZE; i++) {
            pointsVector.insertLast();
            pointsAccessor.setX(random.nextDouble());
            pointsAccessor.setY(random.nextDouble());
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void pointsObject() {
        double result = 0.0;
        for (int i = 0; i < SIZE; i++) {
            result += pointsList.get(i).square();
        }
        publishResult = result;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void pointsStruct() {
        double result = 0.0;
        for (int i = 0; i < SIZE; i++) {
            pointsAccessor.current(i);
            result += pointsAccessor.square();
        }
        publishResult = result;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void pointsIndexedStruct() {
        double result = 0.0;
        for (int i = 0; i < SIZE; i++) {
            result += pointsAccessor.square(i);
        }
        publishResult = result;
    }

    public static void main(String args[]) throws RunnerException {
        new Runner(new OptionsBuilder().include(Benchmarks.class.getSimpleName()).forks(1).build()).run();
    }
}
