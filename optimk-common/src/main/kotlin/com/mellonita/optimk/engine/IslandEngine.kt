package com.mellonita.optimk.engine

import com.mellonita.optimk.*
import java.util.concurrent.atomic.AtomicLong


/**
 * Island Model Engine
 */
class IslandEngine<T>(
    private val problem: Problem<T>,
    private val migrationInterval: Int,
    optimizers: Set<Optimizer>,
    goal: Int,
    monitor: (IterationInfo<T>) -> Boolean
) : Engine<T>(goal, monitor) {

    private val openIslands: Set<Optimizer> = optimizers.filter { it is OpenBorder }.toSet()
    private val currentGeneration: Map<Optimizer, Array<DoubleArray>> = optimizers.associateWith { it.initialize() }

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

        do {
            itrCounter++
            if (itrCounter.rem(migrationInterval) == 0L)
                migrate()

        } while (true)

        TODO("Not yet implemented")
    }

    /**
     * Single objective function evaluation
     */
    override fun evaluate(candidate: DoubleArray): Double {
        evalCounter.incrementAndGet()

        val actualCandidate = problem.decode(candidate)
        var fitness = Double.MAX_VALUE

        if (problem.isFeasible(actualCandidate))
            fitness = goal * problem.objective(actualCandidate)
        if (fitness < bestFitness) {
            bestFitness = fitness
            bestSolution = candidate
        }
        return fitness
    }

    private fun migrate() {

    }
}