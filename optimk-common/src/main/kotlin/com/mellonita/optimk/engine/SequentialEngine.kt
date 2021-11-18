package com.mellonita.optimk.engine

import com.mellonita.optimk.*


/**
 * Engine
 */
class SequentialEngine<T>(
    private val problem: Problem<T>,
    private val goal: Int,
    optimizer: Optimizer,
    monitor: (info: IterationInfo<T>) -> Boolean
) : Engine<T>(optimizer, monitor) {

    private var bestSolution: DoubleArray = doubleArrayOf()
    private var bestFitness: Double = Double.MAX_VALUE

    private var itrCounter: Long = 0
    private var evalCounter: Long = 0
    private var startTime: Long by InitOnceProperty()


    /**
     *
     */
    override fun evaluate(candidate: DoubleArray): Double {
        evalCounter++

        val actualCandidate = problem.decode(candidate)
        var fitness = Double.MAX_VALUE

        if (problem.isFeasible(actualCandidate))
            fitness = goal * problem.objective(actualCandidate)
        if (fitness < bestFitness) {
            bestFitness = fitness
            bestSolution = candidate
        }
        return fitness
    }

    /**
     *
     */
    override fun optimize(): IterationInfo<T> {
        this.startTime = System.currentTimeMillis()

        var population = optimizer.initialize()
        this.bestSolution = population[0]
        var info: IterationInfo<T>

        do {
            itrCounter++
            val fitnessValues = population.map { evaluate(it) }.toDoubleArray()
            info = IterationInfo(
                bestSolution = problem.decode(this.bestSolution),
                bestFitness = goal * bestFitness,
                evaluation = evalCounter,
                iteration = itrCounter,
                time = System.currentTimeMillis() - startTime,
                min = fitnessValues.minOf { it },
                max = fitnessValues.maxOf { it },
                average = fitnessValues.average()
            )
            population = optimizer.iterate(population, fitnessValues)
        } while (!monitor(info))

        return info
    }
}


