@file:Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk

import kotlin.random.Random


/**
 * Abstract optimizer class
 *
 * @param params Parameters
 */
abstract class Optimizer(val params: Map<String, Any>) {

    val rng = Random(1994L)

    /**
     * Fill the currentGeneration with a new generation of solutions
     */
    abstract fun iterate(currentGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray>


    /**
     * Initialize the population
     */
    abstract fun initialize(): Array<DoubleArray>

    companion object {
        const val PARAM_DIMENSIONS = "dimensions"
        const val PARAM_POPULATION = "population"
    }
}