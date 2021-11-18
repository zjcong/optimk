@file:Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk

/**
 *
 */
interface AcceptImmigrant


/**
 * Abstract optimizer class
 *
 */
abstract class Optimizer {

    var objective: (DoubleArray) -> Double by InitOnceProperty()

    /**
     *
     */
    abstract fun iterate(previousGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray>


    /**
     * Initialize the population
     */
    abstract fun initialize(): Array<DoubleArray>


}