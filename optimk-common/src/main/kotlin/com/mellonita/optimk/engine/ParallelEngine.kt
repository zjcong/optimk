package com.mellonita.optimk.engine

import com.mellonita.optimk.*
import java.util.*
import java.util.concurrent.atomic.AtomicLong


/**
 * Engine
 */
class ParallelEngine<T>(
    private val problem: Problem<T>,
    private val goal: Int,
    optimizer: Optimizer,
    monitor: (info: IterationInfo<T>) -> Boolean
) : Engine<T>(optimizer, monitor) {

    private var bestSolution: DoubleArray by SynchronizedProperty(doubleArrayOf())
    private var bestFitness: Double by SynchronizedProperty(Double.MAX_VALUE)

    private var itrCounter: Long = 0
    private var evalCounter: AtomicLong = AtomicLong(0)
    private var startTime: Long by InitOnceProperty()


    /**
     *
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

    /**
     *
     */
    override fun optimize(): IterationInfo<T> {
        this.startTime = System.currentTimeMillis()

        var population = optimizer.initialize()
        this.bestSolution = population[0]
        var info: IterationInfo<T>

        do {
            itrCounter++
            val fitnessValues = population.toList().parallelStream().mapToDouble { evaluate(it) }.toArray()


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
            population = optimizer.iterate(population, fitnessValues)
        } while (!monitor(info))

        return info
    }
}


