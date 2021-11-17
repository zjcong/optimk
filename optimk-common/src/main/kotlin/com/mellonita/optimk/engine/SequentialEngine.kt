@file:Suppress("unused")

package com.mellonita.optimk.engine

import com.mellonita.optimk.*
import kotlin.reflect.KClass


/**
 * Engine
 */
class SequentialEngine<T>(
    optimizerClass: KClass<out Optimizer>,
    optimizerParameters: Map<String, Any>,
    private val problem: Problem<T>,
    private val goal: GoalType,
    private val stop: (itr: Long, fitness: Double, eval: Long, start: Long) -> Boolean,
    private val monitor: (population: Array<DoubleArray>, fitness: DoubleArray) -> Unit = { _, _ -> }
) : Engine<T>(optimizerClass, optimizerParameters) {

    private var bestSolution: DoubleArray = doubleArrayOf()
    private var bestFitness: Double = Double.NaN

    private var itrCounter: Long = 0
    private var evalCounter: Long = 0


    /**
     *
     *
     */
    private fun evaluate(candidate: DoubleArray): Double {
        evalCounter++
        val actualCandidate = problem.decode(candidate)

        val fitness =
            if (goal == GoalType.Maximize) {
                if (problem.isFeasible(actualCandidate))
                    -problem.objective(actualCandidate)
                else
                    Double.MAX_VALUE
            } else {
                if (problem.isFeasible(actualCandidate))
                    problem.objective(actualCandidate)
                else Double.MAX_VALUE
            }

        if (bestFitness.isNaN()) {
            bestFitness = fitness
            bestSolution = candidate
            return fitness
        }

        if (fitness < bestFitness) {
            bestFitness = fitness
            bestSolution = candidate
        }

        return fitness
    }


    private fun iterate(population: Array<DoubleArray>): Array<DoubleArray> {
        itrCounter++
        val fitnessValues = population.map { evaluate(it) }.toDoubleArray()
        monitor(population, fitnessValues)
        return optimizer.iterate(population, fitnessValues)
    }

    /**
     *
     */
    override fun optimize(): OptimizationResult<T> {
        val startTime = System.currentTimeMillis()

        var population = optimizer.initialize()

        do {
            population = iterate(population)
        } while (!stop(
                itrCounter,
                if (goal == GoalType.Maximize) -bestFitness else bestFitness,
                evalCounter,
                startTime
            )
        )
        return OptimizationResult(
            problem.decode(bestSolution),
            if (goal == GoalType.Maximize) -bestFitness else bestFitness,
            itrCounter,
            System.currentTimeMillis() - startTime,
            evalCounter
        )
    }
}

