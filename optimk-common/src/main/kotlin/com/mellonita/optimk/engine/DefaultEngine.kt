@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


/**
 * Goal types
 *
 */
enum class GoalType { Maximize, Minimize }

/**
 * Stopping condition
 *
 */
enum class StopType { GoalReached, Timeout, ItrReached, EvalReached }

/**
 * Stopping criterion
 */
data class StopCriterion(
    val type: StopType,
    val value: Number
)

/**
 * Optimization result
 *
 * @property solution Best solution
 * @property fitness Fitness value of the best solution
 * @property iteration Total iterations
 * @property time Run time in milliseconds
 */
data class OptimizationResult<T>(
    val solution: T,
    val fitness: Double,
    val iteration: Long,
    val time: Long,
    val eval: Long
) {
    override fun toString(): String {

        val solutionString = when (solution) {
            is IntArray -> solution.joinToString(", ")
            is DoubleArray -> solution.joinToString(", ")
            is LongArray -> solution.joinToString(",")
            else -> solution.toString()
        }

        return StringBuilder()
            .appendLine("Optimization terminated")
            .appendLine("Optimization has run for $iteration iterations, in $time milliseconds")
            .appendLine("Objective function has been evaluated $eval times")
            .appendLine("Best solution has fitness value of $fitness")
            .appendLine("Best solution is: $solutionString")
            .toString()
    }
}


/**
 * Engine
 */
abstract class DefaultEngine<T>(
    val problem: Problem<T>,
    val stoppingCriteria: Set<StopCriterion>,
    val goalType: GoalType,
    val optimizerClass: KClass<out Optimizer>,
    val optimizerParameters: Map<String, Any>
) : Engine<T> {

    protected var bestSolution: DoubleArray = doubleArrayOf()
    protected var bestFitness: Double = Double.NaN

    protected var startTime: Long = Long.MAX_VALUE
    protected var iterationCounter: Long = 0
    protected var objectiveCounter: Long = 0


    protected val optimizer: Optimizer

    init {
        //TODO add constructor check
        optimizer = optimizerClass.primaryConstructor!!.call(optimizerParameters, ::evaluate)
    }


    /**
     *
     *
     */
    open fun evaluate(candidate: DoubleArray): Double {
        objectiveCounter++
        val actualCandidate = problem.decode(candidate)

        val fitness =
            if (goalType == GoalType.Maximize) {
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

    /**
     * Check if any stop criterion is met
     */
    open fun shouldStop(): Boolean = stoppingCriteria.any { criterion ->
        when (criterion.type) {
            StopType.Timeout -> (System.currentTimeMillis() - startTime) >= criterion.value.toLong()
            StopType.ItrReached -> iterationCounter >= criterion.value.toLong()
            StopType.EvalReached -> objectiveCounter >= criterion.value.toLong()
            StopType.GoalReached ->
                if (goalType == GoalType.Maximize) bestFitness <= -criterion.value.toDouble()
                else bestFitness <= criterion.value.toDouble()
        }
    }
}


