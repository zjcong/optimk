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


package com.mellonita.optimk.core.sampler

import com.mellonita.optimk.core.*

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Genetic Algorithm, based on design principles of Biased Random Key Genetic Algorithm (BRKGA)
 *
 */
public class BiasedGeneticAlgorithm @JvmOverloads constructor(
    d: Int,
    p: Int,
    private val bias: Double = 0.70,
    elites: Double = 0.25,
    mutants: Double = 0.2,
    rng: Random = Random(0)
) : Optimizer(d, p, rng), OpenBorder {

    private val nElites: Int = (p * elites).roundToInt()
    private val nMutants: Int = (p * mutants).roundToInt()

    init {
        require(d > 0) { "Number of dimensions ($d) must be greater than zero" }
        require(p > 4) { "Population size ($p) must be greater than 4" }
        require(this.nElites > 0) { "Elites ($nElites) must be greater than 0" }
        require(this.nMutants > 0) { "Mutant ($nMutants) must be greater than 0" }
        require(this.nElites + this.nMutants < p) { "Sum of elites and mutants exceeds population size" }
    }

    public constructor(d: Int, p: Int, rng: Random) : this(d, p, 0.7, 0.25, 0.20, rng)

    /**
     *
     */
    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {

        val nextGeneration: Array<DoubleArray> = Array(this.p) { DoubleArray(0) }

        val indicesSorted = fitness
            .withIndex()
            .sortedBy { it.value }
            .map { it.index }

        // Copy elites
        (0 until nElites).forEach { i -> nextGeneration[i] = population[indicesSorted[i]] }

        // Generate Mutants
        (0 until nMutants).forEach { i ->
            nextGeneration[i + nElites] = DoubleArray(d) { rng.nextDouble() }
        }

        // Crossover
        ((nElites + nMutants) until this.p).forEach { s ->
            val eliteParent = population[indicesSorted.subList(0, nElites)[rng.nextInt(0, nElites)]]
            val normalParent = population[rng.nextInt(nElites, this.p)]
            val child = DoubleArray(d) { i ->
                if (rng.nextDouble() < bias) eliteParent[i] else normalParent[i]
            }
            nextGeneration[s] = child
        }
        return nextGeneration
    }

}