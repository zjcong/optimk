@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.mellonita.optimk.common

import java.lang.reflect.Type
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
enum class StopType { GoalReached, Timeout, IterationReached; }

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
    val time: Long
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
            .appendLine("Optimization has run for $iteration iterations and $time milliseconds")
            .appendLine("Best solution has fitness value of $fitness")
            .appendLine("Best solution is: $solutionString")
            .toString()
    }
}


/**
 * Engine
 */
abstract class Engine<T>(
    val problem: Problem<T>,
    val stoppingCriteria: Set<StopCriterion>,
    val goalType: GoalType,
    val optimizerClass: KClass<out Optimizer>,
    optimizerParameters: Map<String, Any>
) {

    protected var bestFitness: Double = Double.NaN
    protected var startTime: Long = Long.MAX_VALUE
    protected var iterationCount: Long = 0

    init {
        optimizerClass.primaryConstructor!!.call(optimizerParameters)
    }

    /**
     * Perform optimization
     */
    abstract fun optimize(): OptimizationResult<T>


    /**
     * Check if any stop criterion is met
     */
    fun shouldStop(): Boolean = stoppingCriteria.any { criterion ->
        when (criterion.type) {
            StopType.Timeout -> (System.currentTimeMillis() - startTime) > criterion.value.toLong()
            StopType.IterationReached -> iterationCount > criterion.value.toLong()
            StopType.GoalReached -> {
                if (goalType == GoalType.Maximize) bestFitness >= criterion.value.toDouble()
                else bestFitness >= criterion.value.toDouble()
            }
        }
    }
}




