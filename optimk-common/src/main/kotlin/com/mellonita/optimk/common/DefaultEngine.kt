package com.mellonita.optimk.common

import kotlin.reflect.KClass

/**
 * Default engine
 */
open class DefaultEngine<T>(
    problem: Problem<T>,
    stoppingCriterion: StoppingCriterion,
    optimizerClass: KClass<out Optimizer>,
    params: Map<String, Any>
) : Engine<T>(problem, stoppingCriterion, optimizerClass, params) {

    private var iterationCounter: Long = 0
    private var starTime: Long = 0
    private var bestFitness: Double = Double.MAX_VALUE
    private var stillCounter: Long = 0
    private var bestCandidate: T? = null

    /**
     *
     */
    override fun optimize(): OptimizationResult<T> {
        starTime = System.currentTimeMillis()
        val stopReason: String

        while (true) {
            val (shouldStop, reason) = isStopping()
            if (shouldStop) {
                stopReason = reason
                break
            }

            val lastBest = bestFitness

            optimizer.iterate()

            if (bestFitness < lastBest) {
                stillCounter = 0
            } else {
                stillCounter++
            }
            iterationCounter++
        }

        return OptimizationResult(bestCandidate!!, bestFitness, stopReason, iterationCounter)
    }

    /**
     *
     */
    override fun proxyFitnessFunc(keys: DoubleArray): Double {
        val fitness = super.proxyFitnessFunc(keys)
        if (fitness < bestFitness) {
            bestFitness = fitness
            bestCandidate = problem.decoder(keys)
        }
        return super.proxyFitnessFunc(keys)
    }

    /**
     *
     */
    private fun isStopping(): Pair<Boolean, String> {
        if (iterationCounter >= stoppingCriterion.afterIteration) {
            return Pair(true, "Max iteration (${stoppingCriterion.afterIteration}) reached")
        }
        if (stillCounter >= stoppingCriterion.afterFitnessUnchangedFor) {
            return Pair(true, "Fitness remains unchanged for ${stoppingCriterion.afterFitnessUnchangedFor} iterations")
        }
        if (bestFitness <= stoppingCriterion.whenFitnessReach) {
            return Pair(true, "Fitness goal (${stoppingCriterion.whenFitnessReach}) reached")
        }
        if ((System.currentTimeMillis() - starTime) >= stoppingCriterion.afterMilliseconds) {
            return Pair(true, "Execution timeout (${stoppingCriterion.afterMilliseconds} milliseconds)")
        }
        return Pair(false, "")
    }
}