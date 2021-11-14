package com.mellonita.optimk.common

import kotlin.reflect.KClass

/**
 * Stopping criteria
 */
data class StoppingCriterion(
    val whenFitnessReach: Double = -1.0 * Double.MAX_VALUE,
    val afterIteration: Int = Int.MAX_VALUE,
    val afterFitnessUnchangedFor: Int = Int.MAX_VALUE,
    val afterMilliseconds: Long = 86400000L
)

/**
 * Optimization result
 */
data class OptimizationResult<T>(
    val solution: T,
    val fitness: Double,
    val reason: String,
    val iteration: Long
) {
    override fun toString(): String {
        return StringBuilder().apply {
            appendLine("Optimization terminated:")
            appendLine("Reason: $reason")
            appendLine("Best solution (fitness: $fitness): $solution")
            appendLine("Iteration: $iteration")
        }.toString()
    }
}


/**
 *
 */
abstract class Engine<T>(
    val problem: Problem<T>,
    val stoppingCriterion: StoppingCriterion,
    optimizerClass: KClass<out Optimizer>,
    params: Map<String, Any>
) {
    /**
     * Optimizer instance
     */
    protected val optimizer: Optimizer

    init {
        // add dimensions into parameters
        val p: Map<String, Any> =
            if (params.containsKey("dimensions"))
                params
            else
                params.plus(Pair("dimensions", problem.dimensions))

        //Construct optimizer instance
        optimizer = optimizerClass.constructors.first().call(p, ::proxyFitnessFunc)
    }

    /**
     * Surrogate fitness func
     */
    open fun proxyFitnessFunc(keys: DoubleArray): Double {
        // Decode keys
        val decoded = problem.decoder(keys)
        // Evaluate candidate
        return if (problem.feasible(decoded)) {
            problem.fitness(decoded)
        } else {
            Double.MAX_VALUE
        }
    }

    /**
     * Perform optimization
     */
    abstract fun optimize(): OptimizationResult<T>

}



