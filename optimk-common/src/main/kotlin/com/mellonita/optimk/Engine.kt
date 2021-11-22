package com.mellonita.optimk

import java.util.concurrent.atomic.AtomicLong

/**
 *
 */
data class IterationInfo<T>(
    val bestSolution: T,
    val iteration: Long,
    val evaluation: Long,
    val time: Long,
    val bestFitness: Double
)


enum class Goal(private val value: Int) {
    Maximize(-1),
    Minimize(1);

    operator fun times(d: Double) = this.value.toDouble() * d
}

/**
 * Engine
 */
abstract class Engine<T>(
    val problem: Problem<T>,
    val goal: Goal,
    val monitor: (info: IterationInfo<T>) -> Boolean
) {

    val evalCounter: AtomicLong = AtomicLong(0)

    /**
     * Perform optimization
     */
    abstract fun optimize(): IterationInfo<T>


    /**
     * Single objective function evaluation
     */
    open fun evaluate(candidate: DoubleArray): Double {
        evalCounter.incrementAndGet()
        if (candidate.any { it !in (0.0).rangeTo(1.0) })
            return Double.MAX_VALUE
        val actualCandidate = problem.decode(candidate)
        return if (problem.isFeasible(actualCandidate))
            goal * problem.objective(actualCandidate)
        else
            Double.MAX_VALUE
    }


}