package com.mellonita.optimk.common


/**
 * Problem Interface
 */
abstract class Problem<T> {
    abstract val dimensions: Int

    abstract fun decoder(randomKeys: DoubleArray): T
    abstract fun feasible(candidate: T): Boolean
    abstract fun fitness(candidate: T): Double
}