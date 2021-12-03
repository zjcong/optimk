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

package com.mellonita.optimk.core

import java.io.Serializable
import kotlin.random.Random


/**
 * Migratable marker interface
 */
public interface Stateless : Serializable


/**
 * Optimizer
 * @property dimensions Dimensionality
 * @property populationSize Size of population
 * @property rng Random number generator
 */
public abstract class Sampler(
    public val dimensions: Int,
    public val populationSize: Int,
    public val rng: Random
) : Serializable {

    init {
        require(dimensions > 0) { "Dimensions must be greater than 0" }
        require(populationSize > 0) { "Population must be greater than 0" }
        //Check constructor
        val constructors = this.javaClass.constructors
        val requiredConstructor = constructors.any {
            val parameters = it.parameters
            return@any (parameters.size == 3)
                    && parameters.map { p -> p.type.toString() }
                .containsAll(listOf("int", "int", "class kotlin.random.Random"))
        }
        require(requiredConstructor) { "Ill implementation of sampler: ${this.javaClass.simpleName}" }
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
        return Array(populationSize) { DoubleArray(dimensions) { rng.nextDouble() } }
    }


    /**
     * Initialize with guess
     * @param init guesses
     * @return an initial population
     */
    public open fun initialize(init: Array<DoubleArray>): Array<DoubleArray> {
        if (init.size >= populationSize) return init.sliceArray(0 until populationSize)
        return Array(populationSize) {
            if (it < init.size) init[it]
            else DoubleArray(dimensions) { rng.nextDouble() }
        }
    }
}
