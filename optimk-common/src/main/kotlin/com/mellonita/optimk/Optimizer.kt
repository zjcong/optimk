@file:Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk


/**
 * Abstract optimizer class
 *
 * @param params Parameters
 */
abstract class Optimizer(
    val params: Map<String, Any>,
) {

    /**
     * Fill the currentGeneration with a new generation of solutions
     */
    abstract fun iterate(currentGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray>


    /**
     * Initialize the population
     */
    abstract fun initialize(): Array<DoubleArray>
}