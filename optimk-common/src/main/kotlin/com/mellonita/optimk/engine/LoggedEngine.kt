package com.mellonita.optimk.engine

import com.mellonita.optimk.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.reflect.KClass


/**
 *
 */
data class ProgressLog(
    val iteration: Long,
    val globalBestFitness: Double,
    val minFitness: Double,
    val maxFitness: Double,
    val mean: Double,
    val std: Double,
)

/**
 *
 */
class LoggedEngine<T>(
    problem: Problem<T>,
    stoppingCriteria: Set<StopCriterion>,
    goalType: GoalType,
    optimizerClass: KClass<out Optimizer>,
    optimizerParams: Map<String, Any>,
    private val logInterval: Int,
    private val logger: (ProgressLog) -> Unit
) : BasicEngine<T>(problem, stoppingCriteria, goalType, optimizerClass, optimizerParams) {


    private val log: MutableList<ProgressLog> = mutableListOf()

    /**
     *
     */
    override fun iterate(population: Array<DoubleArray>): Array<DoubleArray> {
        iterationCounter++
        val fitnessValues = population.map { evaluate(it) }.toDoubleArray()
        if ((iterationCounter - 1).rem(logInterval) == 0L) {
            val max = fitnessValues.maxOf { it }
            val min = fitnessValues.minOf { it }
            val mean = fitnessValues.average()
            val std = sqrt(fitnessValues.sumOf { it - mean }.pow(2) / fitnessValues.size.toDouble())

            val entry = ProgressLog(
                iteration = iterationCounter,
                globalBestFitness = if (goalType == GoalType.Maximize) -bestFitness else bestFitness,
                minFitness = min,
                maxFitness = max,
                mean = mean,
                std = std
            )
            log.add(entry)
            logger(entry)
        }
        return optimizer.iterate(population, fitnessValues)
    }
}