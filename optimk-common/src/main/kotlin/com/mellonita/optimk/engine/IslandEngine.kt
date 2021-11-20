package com.mellonita.optimk.engine

import com.mellonita.optimk.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Island
 */
private data class Island(
    val optimizer: Optimizer,
    var currentGeneration: Array<DoubleArray>,
    var fitnessValues: DoubleArray,
    var bestFitness: Double,
    var bestSolution: DoubleArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Island

        if (optimizer != other.optimizer) return false
        if (!currentGeneration.contentDeepEquals(other.currentGeneration)) return false
        if (!fitnessValues.contentEquals(other.fitnessValues)) return false
        if (bestFitness != other.bestFitness) return false
        if (!bestSolution.contentEquals(other.bestSolution)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = optimizer.hashCode()
        result = 31 * result + currentGeneration.contentDeepHashCode()
        result = 31 * result + fitnessValues.contentHashCode()
        result = 31 * result + bestFitness.hashCode()
        result = 31 * result + bestSolution.contentHashCode()
        return result
    }

}


/**
 * Island Model Engine
 */
class IslandEngine<T>(
    private val problem: Problem<T>,
    optimizers: Set<Optimizer>,
    goal: Goal,
    private val migrationInterval: Int,
    monitor: (IterationInfo<T>) -> Boolean
) : Engine<T>(goal, monitor) {

    //Islands
    private val islands: Set<Island> = optimizers.map {
        Island(
            optimizer = it,
            currentGeneration = it.initialize(),
            fitnessValues = doubleArrayOf(),
            bestFitness = Double.MAX_VALUE,
            bestSolution = doubleArrayOf()
        )
    }.toSet()

    private var bestSolution: DoubleArray by SynchronizedProperty(doubleArrayOf())
    private var bestFitness: Double by SynchronizedProperty(Double.MAX_VALUE)

    private var itrCounter: Long = 0
    private var evalCounter: AtomicLong = AtomicLong(0)
    private var startTime: Long by InitOnceProperty()


    /**
     * Perform optimization
     */
    override fun optimize(): IterationInfo<T> {
        startTime = System.currentTimeMillis()
        var info: IterationInfo<T>

        do {
            itrCounter++

            islands.parallelStream().forEach { islandEvaluate(it) }

            val min = islands.minByOrNull { it.bestFitness }!!

            if (min.bestFitness < bestFitness) {
                bestFitness = min.bestFitness
                bestSolution = min.bestSolution
            }

            if (itrCounter.rem(migrationInterval) == 0L) {
                migrate()
            }

            info = IterationInfo(
                bestSolution = problem.decode(bestSolution),
                bestFitness = goal * bestFitness,
                evaluation = evalCounter.get(),
                iteration = itrCounter,
                time = System.currentTimeMillis() - startTime,
                min = islands.minOf { it.bestFitness },
                max = islands.maxOf { it.bestFitness },
                average = islands.map { it.bestFitness }.average()
            )

            islands.parallelStream()
                .forEach { it.currentGeneration = it.optimizer.iterate(it.currentGeneration, it.fitnessValues) }

        } while (!monitor(info))

        return info
    }


    /**
     * Single objective function evaluation
     */
    override fun evaluate(candidate: DoubleArray): Double {
        evalCounter.incrementAndGet()
        val actualCandidate = problem.decode(candidate)
        return if (problem.isFeasible(actualCandidate))
            goal * problem.objective(actualCandidate)
        else
            Double.MAX_VALUE
    }

    /**
     *
     */
    private fun islandEvaluate(island: Island) {
        island.fitnessValues = island.currentGeneration.map { evaluate(it) }.toDoubleArray()
        val min = island.fitnessValues.withIndex().minByOrNull { it.value }!!
        island.bestFitness = min.value
        island.bestSolution = island.currentGeneration[min.index]
    }

    /**
     *
     */
    private fun migrate() {
        val from = islands.random()
        val to = islands.filter { it.optimizer is OpenBorder }.random()
        val worst = to.fitnessValues.withIndex().maxByOrNull { it.value }!!
        to.currentGeneration[worst.index] = from.bestSolution
        to.fitnessValues[worst.index] = from.bestFitness
    }
}