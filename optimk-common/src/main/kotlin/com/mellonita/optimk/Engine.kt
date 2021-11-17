package com.mellonita.optimk

import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


/**
 * Goal types
 *
 */
enum class GoalType { Maximize, Minimize }


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
 *
 */
abstract class Engine<T>(
    optimizerClass: KClass<out Optimizer>,
    optimizerParameters: Map<String, Any>
) {


    protected val optimizer: Optimizer = optimizerClass.primaryConstructor!!.call(optimizerParameters)

    /**
     * Perform optimization
     */
    abstract fun optimize(): OptimizationResult<T>
}