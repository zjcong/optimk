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


package com.mellonita.optimk.optimizer

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 *
 */
public class BiasedGeneticAlgorithm @JvmOverloads constructor(
    d: Int,
    p: Int,
    private val bias: Double = 0.70,
    private val elites: Int = (p * 0.25).roundToInt(),
    private val mutants: Int = (p * 0.2).roundToInt(),
    rng: Random = Random(0)
) : Optimizer(d, p, rng), OpenBorder {


    init {
        require(d > 0) { "Number of dimensions ($d) must be greater than zero" }
        require(p > 4) { "Population size ($p) must be greater than 4" }
        require(elites > 0) { "Elites ($elites) must be greater than 0" }
        require(mutants > 0) { "Mutant ($mutants) must be greater than 0" }
        require(elites + mutants < p) { "Sum of elites and mutants exceeds population size" }
    }

    /**
     *
     *
     */
    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {

        val nextGeneration: Array<DoubleArray> = Array(p) { DoubleArray(0) }

        val indicesSorted = fitness
            .withIndex()
            .sortedBy { it.value }
            .map { it.index }

        // Copy elites
        (0 until elites).forEach { i -> nextGeneration[i] = population[indicesSorted[i]] }

        // Generate Mutants
        (0 until mutants).forEach { i -> nextGeneration[i + elites] = DoubleArray(d) { rng.nextDouble() } }

        // Crossover
        ((elites + mutants) until p).forEach { s ->
            val eliteParent = population[indicesSorted.subList(0, elites)[rng.nextInt(0, elites)]]
            val normalParent = population[rng.nextInt(elites, p)]
            val child =
                DoubleArray(d) { i -> if (rng.nextDouble() < bias) eliteParent[i] else normalParent[i] }
            nextGeneration[s] = child
        }

        return nextGeneration
    }

}