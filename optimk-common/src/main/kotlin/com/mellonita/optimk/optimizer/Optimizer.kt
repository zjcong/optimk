@file:Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk.optimizer

import java.io.Serializable
import kotlin.random.Random


/**
 * Indicate this optimizer accepts immigrant
 */
public interface OpenBorder : Serializable


/**
 * Optimizer
 * @property d Dimensionality
 * @property p Size of population
 * @property rng Random number generator
 */
public abstract class Optimizer(
    public val d: Int,
    public val p: Int,
    public val rng: Random
) : Serializable {

    init {
        require(d > 1) { "Dimensions (d) must be greater than 1" }
        require(p > 1) { "Population (p) must be greater than 1" }
    }

    /**
     * Iterate
     * @param population current pool of solutions
     * @param fitness fitness values of the solutions
     * @return next generation
     */
    public abstract fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray>

    /**
     * Initialize the optimizer
     * @return an initial population
     */
    public open fun initialize(): Array<DoubleArray> {
        return Array(p) { DoubleArray(d) { rng.nextDouble() } }
    }


    /**
     * Initialize with guess
     * @param init guesses
     * @return an initial population
     */
    public open fun initialize(init: Array<DoubleArray>): Array<DoubleArray> {
        if (init.size >= p) return init.sliceArray(0 until p)
        return Array(p) {
            if (it < init.size) init[it]
            else DoubleArray(d) { rng.nextDouble() }
        }
    }
}
