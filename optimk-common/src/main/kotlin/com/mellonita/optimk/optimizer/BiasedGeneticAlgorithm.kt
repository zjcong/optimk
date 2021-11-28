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

import com.mellonita.optimk.OpenBorder
import com.mellonita.optimk.Optimizer
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Genetic Algorithm, based on design principles of Biased Random Key Genetic Algorithm (BRKGA)
 *
 */
public class BiasedGeneticAlgorithm @JvmOverloads constructor(
    dimensionality: Int,
    population: Int,
    private val bias: Double = 0.70,
    elites: Double = 0.25,
    mutants: Double = 0.2,
    rng: Random = Random(0)
) : Optimizer(dimensionality, population, rng), OpenBorder {

    private val nElites: Int = (population * elites).roundToInt()
    private val nMutants: Int = (population * mutants).roundToInt()

    init {
        require(dimensionality > 0) { "Number of dimensions ($dimensionality) must be greater than zero" }
        require(population > 4) { "Population size ($population) must be greater than 4" }
        require(this.nElites > 0) { "Elites ($nElites) must be greater than 0" }
        require(this.nMutants > 0) { "Mutant ($nMutants) must be greater than 0" }
        require(this.nElites + this.nMutants < population) { "Sum of elites and mutants exceeds population size" }
    }

    /**
     *
     */
    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {

        val nextGeneration: Array<DoubleArray> = Array(this.population) { DoubleArray(0) }

        val indicesSorted = fitness
            .withIndex()
            .sortedBy { it.value }
            .map { it.index }

        // Copy elites
        (0 until nElites).forEach { i -> nextGeneration[i] = population[indicesSorted[i]] }

        // Generate Mutants
        (0 until nMutants).forEach { i ->
            nextGeneration[i + nElites] = DoubleArray(dimensionality) { rng.nextDouble() }
        }

        // Crossover
        ((nElites + nMutants) until this.population).forEach { s ->
            val eliteParent = population[indicesSorted.subList(0, nElites)[rng.nextInt(0, nElites)]]
            val normalParent = population[rng.nextInt(nElites, this.population)]
            val child = DoubleArray(dimensionality) { i ->
                if (rng.nextDouble() < bias) eliteParent[i] else normalParent[i]
            }
            nextGeneration[s] = child
        }
        return nextGeneration
    }

}