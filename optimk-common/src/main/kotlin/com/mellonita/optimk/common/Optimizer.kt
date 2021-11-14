@file:Suppress("UNUSED_PARAMETER")

package com.mellonita.optimk.common


/**
 * Abstract optimizer class
 *
 * @param params Parameters
 * @param fitnessFunc Fitness Function
 */

abstract class Optimizer(
    params: Map<String, Any>,
    val fitnessFunc: (DoubleArray) -> Double
) {
    abstract val currentGeneration: Array<DoubleArray>
    abstract fun iterate()
}