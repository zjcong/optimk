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
    abstract fun iterate(): DoubleArray


}

