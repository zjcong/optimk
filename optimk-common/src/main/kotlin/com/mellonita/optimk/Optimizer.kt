@file:Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk

/**
 *
 */
interface OpenBorder


/**
 * Abstract optimizer class
 *
 */
abstract class Optimizer {

    /**
     * Iterate
     * @param currentGeneration current pool of solutions
     * @param fitnessValues fitness values of the solutions
     * @return next generation
     */
    abstract fun iterate(currentGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray>

    /**
     * Initialize the optimizer
     * @return an initial population
     */
    abstract fun initialize(): Array<DoubleArray>
}
