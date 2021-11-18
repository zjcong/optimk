package com.mellonita.optimk.optimizer

import com.mellonita.optimk.AcceptImmigrant
import com.mellonita.optimk.Optimizer
import kotlin.math.roundToInt
import kotlin.random.Random


class BRKGA(

    private val dimensions: Int,
    private val population: Int,

    private val bias: Double = 0.70,
    private val elites: Int = (population * 0.25).roundToInt(),
    private val mutants: Int = (population * 0.2).roundToInt(),
    private val rng: Random = Random(0)

) : Optimizer(), AcceptImmigrant {

    init {
        require(dimensions > 0) { "Number of dimensions ($dimensions) must be greater than zero" }
        require(population > 4) { "Population size ($population) must be greater than 4" }
        require(elites + mutants < population) { "Sum of elites and mutants exceeds population size" }
    }

    /**
     *
     *
     */
    override fun iterate(previousGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray> {
        val nextGeneration: Array<DoubleArray> = Array(population) { DoubleArray(0) }

        val indicesSorted = fitnessValues
            .withIndex()
            .sortedBy { it.value }
            .map { it.index }

        // Copy elites
        (0 until elites).forEach { i -> nextGeneration[i] = previousGeneration[i] }

        // Generate Mutants
        (0 until mutants).forEach { i -> nextGeneration[i + elites] = DoubleArray(dimensions) { rng.nextDouble() } }

        // Crossover
        ((elites + mutants) until population).forEach { s ->
            val eliteParent = previousGeneration[indicesSorted.subList(0, elites)[rng.nextInt(0, elites)]]
            val normalParent = previousGeneration[rng.nextInt(elites, population)]
            val child =
                DoubleArray(dimensions) { i -> if (rng.nextDouble() < bias) eliteParent[i] else normalParent[i] }
            nextGeneration[s] = child
        }

        return nextGeneration
    }

    /**
     * Generate init population
     */
    override fun initialize(): Array<DoubleArray> = Array(population) { DoubleArray(dimensions) { rng.nextDouble() } }

}