package com.mellonita.optimk.engine

import com.mellonita.optimk.*
import java.util.concurrent.atomic.AtomicLong


/**
 * Basic optimization engine
 * @param problem Problem to solve
 * @param goal Goal type, GOAL_MIN or GOAL_MAX
 * @param optimizer Optimizer
 * @param monitor
 */
class DefaultEngine<T>(
    private val problem: Problem<T>,
    private val optimizer: Optimizer,
    goal: Goal,
    monitor: (info: IterationInfo<T>) -> Boolean
) : Engine<T>(goal, monitor) {

    private var bestSolution: DoubleArray = doubleArrayOf()
    private var bestFitness: Double = Double.MAX_VALUE

    private var itrCounter: Long = 0
    private var evalCounter: AtomicLong = AtomicLong(0)
    private var startTime: Long by InitOnceProperty()


    /**
     *
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
    override fun optimize(): IterationInfo<T> {
        this.startTime = System.currentTimeMillis()

        var info: IterationInfo<T>

        var currentGeneration = optimizer.initialize()

        do {
            itrCounter++
            val fitnessValues = currentGeneration.toList().stream().parallel().mapToDouble { evaluate(it) }.toArray()

            val min = fitnessValues.withIndex().minByOrNull { it.value }!!

            if (min.value < bestFitness) {
                bestFitness = min.value
                bestSolution = currentGeneration[min.index]
            }

            info = IterationInfo(
                bestSolution = problem.decode(bestSolution),
                bestFitness = goal * bestFitness,
                evaluation = evalCounter.get(),
                iteration = itrCounter,
                time = System.currentTimeMillis() - startTime,
                min = fitnessValues.minOf { it },
                max = fitnessValues.maxOf { it },
                average = fitnessValues.average()
            )

            currentGeneration = optimizer.iterate(currentGeneration, fitnessValues)

        } while (!monitor(info))

        return info
    }
}


