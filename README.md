# OptimK

OptimK is a mathematical optimization framework written in Kotlin.

## Introduction

OptimK decouples optimization algorithms from the actual problem by encoding solutions with a vector of real number in
the continuous interval of [0,1), users of OptimK are responsible for decoding such vectors into the actual solutions of
the specific problem.

From an optimization algorithm's point of view, it is always optimizing a box constrained problem of which every
dimension is bounded to [0,1); from a user's perspective, a single interface is provided so that any optimization
algorithm implementation that confirms OptimK's design can be employed to solve their problems.

This design facilitates its users to focus on describing the problem instead of the optimization details, it also
liberates the optimization algorithm designers from designing complex mechanisms for the sake of flexibility.

## Architecture

OptimK consists of three core components: __Problem__, __Engine__ and __Optimizer__.

### Problem

__Problem__ is the interface with which users describe their problem:

````kotlin
abstract class Problem<T> {
    abstract val dimensions: Int

    abstract fun decode(randomKeys: DoubleArray): T
    abstract fun feasible(candidate: T): Boolean
    abstract fun fitness(candidate: T): Double
}

````

+ Type parameter __T__ is the actual type of the solution.
+ Property __dimensions__ specifies the number of dimensions of the problem.
+ Function __decode__ maps a vector of real number in the interval of [0,1) (a DoubleArray) into an actual solution of
  type T.
+ Function __feasible__ checks the feasibility of the decoded solution.
+ Function __fitness__ computes the fitness value of the solution

A 5-dimensional Rastrigin function would be implemented as:

````kotlin
val rastrigin5D = object : Problem {
    
}

````

## License

General Public License version 3.

OptimK is __NOT__ allowed to be incorporated into proprietary programs. Please contact the author via
Telegram [@drzjcong](https://telegram.me/drzjcong) if this license does not meet your needs.