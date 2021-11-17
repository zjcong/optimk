package com.mellonita.optimk.engine

import com.mellonita.optimk.*
import kotlin.reflect.KClass

/**
 * Basic engine
 */
open class BasicEngine<T>(
    problem: Problem<T>,
    stoppingCriteria: Set<StopCriterion>,
    goalType: GoalType,
    optimizerClass: KClass<out Optimizer>,
    optimizerParams: Map<String, Any>
) : SequentialEngine<T>(problem, stoppingCriteria, goalType, optimizerClass, optimizerParams) {


    open fun iterate(population: Array<DoubleArray>): Array<DoubleArray> {
        iterationCounter++
        val fitnessValues = population.map { evaluate(it) }.toDoubleArray()
        return optimizer.iterate(population, fitnessValues)
    }

    /**
     *
     */
    override fun optimize(): OptimizationResult<T> {
        startTime = System.currentTimeMillis()

        var population = optimizer.initialize()

        while (!shouldStop()) {
            population = iterate(population)
        }

        return OptimizationResult(
            problem.decode(bestSolution),
            if (goalType == GoalType.Maximize) -bestFitness else bestFitness,
            iterationCounter,
            System.currentTimeMillis() - startTime,
            objectiveCounter
        )
    }

}