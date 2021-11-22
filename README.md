# OptimK

OptimK is a mathematical optimization framework written in Kotlin.

> :warning: **THIS IS A WORK-IN-PROGRESS**

## Introduction

OptimK decouples optimization algorithms from the actual problem by encoding solutions with a vector of real number in
the continuous interval of [0,1), users of OptimK are responsible for decoding such vectors into the actual solutions of
the specific problem.

From an optimization algorithm's point of view, it is always optimizing a box constrained problem of which every
dimension is bounded to [0,1); from a user's perspective, a single interface is provided so that any optimization
algorithm that confirms OptimK's design can be employed to solve their problems.

## Architecture

OptimK consists of three core components: __Problem__, __Engine__ and __Optimizer__.

### Problem

__Problem__ is the interface with which users describe their problems:

````kotlin
 interface Problem<T> {

    fun decode(keys: DoubleArray): T
    fun objective(candidate: T): Double
    fun isFeasible(candidate: T): Boolean = true

}
````

+ Type parameter __T__ is the actual type of the solution.
+ Function __decode__ maps a vector of real number in the interval of [0,1) (a DoubleArray) into an actual solution of
  type T.
+ Function __isFeasible__ checks the feasibility of the decoded solution.
+ Function __objective__ computes the fitness value of the solution

__Example__ : A n-dimensional Rastrigin function would be implemented as:

````kotlin
class Rastrigin(val n: Int) : Problem<DoubleArray> {

    private val A = 10

    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.map { it.valueIn((-5.12).rangeTo(5.12)) }.toDoubleArray()
    }

    override fun objective(candidate: DoubleArray): Double {
        return A * n + candidate.sumOf { x -> x.pow(2) - A * cos(2 * PI * x) }
    }
}
````

### Engine

TODO

### Optimizer

TODO

## License

Unless explicitly stated otherwise, every module of this software is licensed under General Public License version 3.

````
    OptimK is a mathematical optimization framework written in Kotlin.
    Copyright (C) Zijie Cong 2021 
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
````

Please contact the author via Telegram [@drzjcong](https://telegram.me/drzjcong) if this license does not meet your
needs.

## TODO

+ ~~Island Model Engine~~
+ ~~Particle Swamp Optimization~~
+ ~~Differential Evolution~~
+ Restart Engine
+ Server Engine