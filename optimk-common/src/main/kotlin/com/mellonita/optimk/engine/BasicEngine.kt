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
) : DefaultEngine<T>(problem, stoppingCriteria, goalType, optimizerClass, optimizerParams) {


    /**
     *
     */
    override fun optimize(): OptimizationResult<T> {
        startTime = System.currentTimeMillis()

        var population = optimizer.initialize()

        while (!shouldStop()) {
            iterationCounter++
            val fitnessValues = population.map { evaluate(it) }.toDoubleArray()
            population = optimizer.iterate(population, fitnessValues)
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