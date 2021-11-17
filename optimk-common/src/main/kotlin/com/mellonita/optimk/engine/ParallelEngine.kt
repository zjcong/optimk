package com.mellonita.optimk.engine

import com.mellonita.optimk.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


/**
 *
 */
class ParallelEngine<T>(
    optimizerClass: KClass<out Optimizer>,
    optimizerParameters: Map<String, Any>,
    private val problem: Problem<T>,
    private val goal: GoalType,
    private val stop: (itr: Long, fitness: Double, eval: Long, start: Long) -> Boolean,
    private val monitor: (population: Array<DoubleArray>, fitness: DoubleArray) -> Unit = { _, _ -> }
) : Engine<T>(optimizerClass, optimizerParameters) {

    private var itrCounter: Long = 0L
    private var evalCounter: AtomicLong = AtomicLong(0L)
    private var bestFitness: Double by SynchronizedProperty(Double.NaN)
    private var bestSolution: DoubleArray by SynchronizedProperty(doubleArrayOf())


    /**
     *
     */
    private fun evaluate(candidate: DoubleArray): Double {
        evalCounter.incrementAndGet()
        val actualSolution: T = problem.decode(candidate)
        var fitness: Double = Double.MAX_VALUE

        if (problem.isFeasible(actualSolution)) {
            fitness = if (goal == GoalType.Maximize)
                -problem.objective(actualSolution)
            else
                problem.objective(actualSolution)
        }

        if (bestFitness.isNaN()) {
            bestSolution = candidate
            bestFitness = fitness
        } else if (fitness < bestFitness) {
            bestSolution = candidate
            bestFitness = fitness
        }

        return fitness
    }

    /**
     *
     */
    private fun iterate(population: Array<DoubleArray>): Array<DoubleArray> {
        itrCounter++
        val fitnessValues = population
            .toList()
            .parallelStream()
            .mapToDouble { evaluate(it) }
            .toArray()
        monitor(population, fitnessValues)
        return optimizer.iterate(population, fitnessValues)
    }


    /**
     *
     */
    override fun optimize(): OptimizationResult<T> {
        val startTime = System.currentTimeMillis()

        var population = optimizer.initialize()

        do {
            population = iterate(population)
        } while (!stop(
                itrCounter,
                if (goal == GoalType.Maximize) -bestFitness else bestFitness,
                evalCounter.get(),
                startTime
            )
        )

        return OptimizationResult(
            problem.decode(bestSolution),
            if (goal == GoalType.Maximize) -bestFitness else bestFitness,
            itrCounter,
            System.currentTimeMillis() - startTime,
            evalCounter.get()
        )

    }

}


/**
 *
 */
internal class SynchronizedProperty<T>(initValue: T) {
    private var value: T = initValue

    @Synchronized
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    @Synchronized
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}