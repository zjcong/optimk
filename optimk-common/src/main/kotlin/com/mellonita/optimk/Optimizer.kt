@file:Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk

import java.io.Serializable

/**
 *
 */
interface OpenBorder : Optimizer

/**
 *
 */
interface Stateless : Optimizer

/**
 * Optimizer Interface
 *
 */
interface Optimizer : Serializable {

    /**
     * Iterate
     * @param currentGeneration current pool of solutions
     * @param fitnessValues fitness values of the solutions
     * @return next generation
     */
    fun iterate(currentGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray>

    /**
     * Initialize the optimizer
     * @return an initial population
     */
    fun initialize(): Array<DoubleArray>


}
