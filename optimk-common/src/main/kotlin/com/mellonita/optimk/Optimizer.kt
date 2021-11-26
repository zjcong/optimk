/*
 * Copyright (C) Zijie Cong 2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk

import java.io.Serializable
import kotlin.random.Random


/**
 * Migratable marker interface
 */
public interface OpenBorder : Serializable


/**
 * Optimizer
 * @property dimensionality Dimensionality
 * @property population Size of population
 * @property rng Random number generator
 */
public abstract class Optimizer(
    public val dimensionality: Int,
    public val population: Int,
    public val rng: Random
) : Serializable {

    init {
        require(dimensionality > 1) { "Dimensions (d) must be greater than 1" }
        require(population > 1) { "Population (p) must be greater than 1" }
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
        return Array(population) { DoubleArray(dimensionality) { rng.nextDouble() } }
    }


    /**
     * Initialize with guess
     * @param init guesses
     * @return an initial population
     */
    public open fun initialize(init: Array<DoubleArray>): Array<DoubleArray> {
        if (init.size >= population) return init.sliceArray(0 until population)
        return Array(population) {
            if (it < init.size) init[it]
            else DoubleArray(dimensionality) { rng.nextDouble() }
        }
    }
}
