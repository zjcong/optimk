package com.mellonita.optimk

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


enum class Goal(private val value: Int) {
    Maximize(-1),
    Minimize(1);

    operator fun times(d: Double) = this.value.toDouble() * d
}

/**
 * Engine
 */
abstract class Engine<T>(val goal: Goal, val monitor: (info: IterationInfo<T>) -> Boolean) {


    /**
     * Perform optimization
     */
    abstract fun optimize(): IterationInfo<T>

    /**
     *
     */
    abstract fun evaluate(candidate: DoubleArray): Double

}