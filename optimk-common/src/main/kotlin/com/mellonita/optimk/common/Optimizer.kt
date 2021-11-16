@file:Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk.common


/**
 * Abstract optimizer class
 *
 * @param params Parameters
 */
abstract class Optimizer(val params: Map<String, Any>) {

    /**
     * Fill the currentGeneration with a new generation of solutions
     */
    abstract fun nextGeneration(currentGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray>
}