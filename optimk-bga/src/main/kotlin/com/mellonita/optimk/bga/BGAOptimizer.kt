package com.mellonita.optimk.bga

import com.mellonita.optimk.common.Optimizer
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

class BGAOptimizer(
    params: Map<String, Any>,
    fitnessFunc: (DoubleArray) -> Double
) : Optimizer(params, fitnessFunc) {

    private val dimensions: Int by params
    private val population: Int by params
    private val elites: Double by params
    private val mutants: Double by params
    private val bias: Double by params

    private val eliteCount = ceil(population.toDouble() * elites).roundToInt()
    private val mutantCount = ceil(population.toDouble() * mutants).roundToInt()

    private val rndGenerator = Random(0)

    init {
        require(dimensions > 0) { "Dimensions should be greater than 0" }
        require(population > 3) { "Population size should be at least 4" }
        require(elites in 0.10.rangeTo(0.25)) { "Elites should be 0.10-0.25" }
        require(mutants in 0.10.rangeTo(0.30)) { "Mutants should be in range 0.10-0.30" }
        require(bias in 0.50.rangeTo(0.80)) { "Bias should be in range 0.50-0.80" }
    }

    override var currentGeneration: Array<DoubleArray> =
        Array(population) { DoubleArray(dimensions) { rndGenerator.nextDouble() } }

    /**
     *
     */
    override fun iterate() {

        //Evaluate current generation
        val fitness = currentGeneration
            .map { Pair(fitnessFunc(it), it) }
            .sortedBy { it.first }

        val nextGeneration = mutableListOf<DoubleArray>()

        // Copy elites
        nextGeneration.addAll(fitness.subList(0, eliteCount).map { it.second })

        // Crossover
        val children = (0 until (population - eliteCount - mutantCount)).map {
            crossover(nextGeneration.random(), currentGeneration.random())
        }
        nextGeneration.addAll(children)

        // Add mutants
        repeat(mutantCount) {
            nextGeneration.add(DoubleArray(dimensions) { rndGenerator.nextDouble() })
        }

        currentGeneration = nextGeneration.toTypedArray()
    }

    /**
     *
     */
    private fun crossover(elite: DoubleArray, nonElite: DoubleArray): DoubleArray = DoubleArray(dimensions) { i ->
        val b = rndGenerator.nextDouble()
        if (b < bias)
            elite[i]
        else
            nonElite[i]
    }
}