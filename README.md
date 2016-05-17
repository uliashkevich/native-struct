NATIVE STRUCT LIBRARY
=====================

The library helps Java programs to achieve better performance by employing array programming techniques:
   * Collections of objects are represented as typed arrays holding their fields.
   * A set of operations that provides means of efficient data manipulation.

Motivation
----------

There is an impedance mismatch between how developers write their programs and how computers are 
designed to executed those programs. The problem lies in layered memory architecture where each 
next level provides more storage with exponentially increasing latency.

The following table may give you a rough idea about the cost of typical operations 
(from http://norvig.com/21-days.html#answers):
 
| *Operation*          | *Relative latency*
|----------------------|-------------------
| L1 cache access      | 1
| Typical instruction  | 2
| Branch misprediction | 10
| L2 cache access      | 14
| Mutex lock/unlock    | 50
| Main memory access   | 200

Additional problem arises in multi-core and multi-processor systems, where concurrent write and 
read operations may increase latency by causing local cache invalidation.  

Hence we can derive key characteristics of high-performance programs:
   1) They have minimal memory footprint.
   2) They avoid concurrent data access scenarios.
   3) They arrange data to allow consecutive memory access pattern as opposed random access and excessive indirection levels.

Not every program requires maximum performance. It is safe to say that the programmer productivity 
is much more important for the majority of problems.

Yet, there are classes of problems that are very sensitive to performance:
   1) Processing of large amounts of data.
   2) Near real-time data processing.
   3) Computer graphics and video games.
   4) Modeling of physical processes.


Java is not the best language to deal with these types of problems. But it is often becomes the
tool of choice due to its popularity and availability of high quality libraries.

Here is a list of restrictions imposed by Java memory model that prevent achieving maximum performance:
   * Each object is allocated on the heap. There is storage overhead for object header and data alignment to word boundary.
   * Memory allocation is unpredictable. There is no control over data locality.
   * Primitive data types are boxed in collection classes.


Solution
--------

The native-struct library introduces a notion of `struct vector`. It is a collection of data 
objects, where all the properties are held in typed arrays. 


Benchmark
---------

The following micro-benchmark show results of performing very basic computation. Suppose, we are
given a collection of `Point` objects having `x` and `y` coordinates of type `double`. The problem
is to accumulate the sum of squares of the coordinates.
The first benchmark is implemented using `ArrayList<Point>`.
The second and third one employ `StructVector<PointStruct>`.

```
Benchmark                       Mode  Cnt     Score    Error  Units
Benchmarks.pointsObject         avgt   20  2643.381 ± 20.255  us/op
Benchmarks.pointsStruct         avgt   20  1241.162 ±  7.446  us/op
Benchmarks.pointsIndexedStruct  avgt   20  1236.641 ±  9.049  us/op
```

