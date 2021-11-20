package com.mellonita.optimk

const val GOAL_MAX = -1
const val GOAL_MIN = 1

/**
 *
 */
data class IterationInfo<T>(
    val bestSolution: T,
    val iteration: Long,
    val evaluation: Long,
    val time: Long,
    val bestFitness: Double,
    val min: Double,
    val max: Double,
    val average: Double,
)


/**
 * Engine
 */
abstract class Engine<T>(val goal: Int, val monitor: (info: IterationInfo<T>) -> Boolean) {

    init {
        require(goal == GOAL_MAX || goal == GOAL_MIN)
    }

    /**
     * Perform optimization
     */
    abstract fun optimize(): IterationInfo<T>

    /**
     *
     */
    abstract fun evaluate(candidate: DoubleArray): Double

}