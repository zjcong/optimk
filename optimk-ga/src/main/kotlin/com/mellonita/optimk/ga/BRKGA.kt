package com.mellonita.optimk.ga

import com.mellonita.optimk.Optimizer
import kotlin.math.roundToInt
import kotlin.random.Random


class BRKGA(
    params: Map<String, Any>,
    objective: (DoubleArray) -> Double
) : Optimizer(params, objective) {

    private val rnd: Random = Random(0)

    private val dimensions: Int by params
    private val population: Int by params

    private val bias: Double
    private val elites: Int
    private val mutants: Int


    init {
        require(dimensions > 0) { "Number of dimensions ($dimensions) must be greater than zero" }
        require(population > 4) { "Population size ($population) must be greater than 4" }

        bias =
            if (params.containsKey(PARAM_BIAS)) params[PARAM_BIAS] as Double
            else 0.70
        elites =
            if (params.containsKey(PARAM_ELITES)) params[PARAM_ELITES] as Int
            else (population * 0.25).roundToInt()
        mutants =
            if (params.containsKey(PARAM_MUTANTS)) params[PARAM_MUTANTS] as Int
            else (population * 0.2).roundToInt()

        require(elites + mutants < population) { "Sum of elites and mutants exceeds population size" }
    }

    /**
     *
     *
     */
    override fun iterate(currentGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray> {
        val nextGeneration: Array<DoubleArray> = Array(population) { DoubleArray(0) }

        val indicesSorted = fitnessValues
            .withIndex()
            .sortedBy { it.value }
            .map { it.index }

        // Copy elites
        (0 until elites).forEach { i -> nextGeneration[i] = currentGeneration[i] }

        // Generate Mutants
        (0 until mutants).forEach { i -> nextGeneration[i + elites] = DoubleArray(dimensions) { rnd.nextDouble() } }

        // Crossover
        ((elites + mutants) until population).forEach { s ->
            val eliteParent = currentGeneration[indicesSorted.subList(0, elites).random()]
            val normalParent = currentGeneration[rnd.nextInt(elites, population)]
            val child =
                DoubleArray(dimensions) { i -> if (rnd.nextDouble() < bias) eliteParent[i] else normalParent[i] }
            nextGeneration[s] = child
        }
        return nextGeneration
    }


    /**
     * Generate init population
     */
    override fun initialize(): Array<DoubleArray> = Array(population) { DoubleArray(dimensions) { rnd.nextDouble() } }


    companion object {
        const val PARAM_DIMENSIONS = "dimensions"
        const val PARAM_POPULATION = "population"
        const val PARAM_BIAS = "bias"
        const val PARAM_ELITES = "elites"
        const val PARAM_MUTANTS = "mutants"
    }
}