package com.mellonita.optimk.engine

import com.mellonita.optimk.*


/**
 * Engine
 */
class SequentialEngine<T>(
    private val problem: Problem<T>,
    private val goal: GoalType,
    optimizer: Optimizer,
    monitor: Monitor
) : Engine<T>(optimizer, monitor) {

    private var bestSolution: DoubleArray = doubleArrayOf()
    private var bestFitness: Double = Double.NaN

    private var itrCounter: Long = 0
    private var evalCounter: Long = 0
    private var startTime: Long by InitOnceProperty()


    constructor(
        problem: Problem<T>,
        goal: GoalType,
        optimizer: Optimizer,
        stop: (info: IterationInfo) -> Boolean = { it.time > 3000 },
        monitor: (info: IterationInfo, population: Array<DoubleArray>, fitness: DoubleArray) -> Unit = { _, _, _ -> ; }
    ) : this(problem, goal, optimizer, object : Monitor {
        override fun stop(info: IterationInfo): Boolean {
            return stop(info)
        }

        override fun report(info: IterationInfo, population: Array<DoubleArray>, fitnessValues: DoubleArray) {
            monitor(info, population, fitnessValues)
        }
    })

    /**
     *
     *
     */
    override fun evaluate(candidate: DoubleArray): Double {
        evalCounter++
        val actualCandidate = problem.decode(candidate)

        val fitness =
            if (goal == GoalType.Maximize) {
                if (problem.isFeasible(actualCandidate))
                    -problem.objective(actualCandidate)
                else
                    Double.MAX_VALUE
            } else {
                if (problem.isFeasible(actualCandidate))
                    problem.objective(actualCandidate)
                else Double.MAX_VALUE
            }

        if (bestFitness.isNaN()) {
            bestFitness = fitness
            bestSolution = candidate
            return fitness
        }

        if (fitness < bestFitness) {
            bestFitness = fitness
            bestSolution = candidate
        }

        return fitness
    }

    /**
     *
     */
    private fun iterate(population: Array<DoubleArray>): Pair<Array<DoubleArray>, IterationInfo> {
        itrCounter++
        val fitnessValues = population.map { evaluate(it) }.toDoubleArray()

        val info = IterationInfo(
            bestFitness = if (goal == GoalType.Maximize) -bestFitness else bestFitness,
            evaluation = evalCounter,
            iteration = itrCounter,
            time = System.currentTimeMillis() - startTime
        )
        monitor.report(info, population, fitnessValues)

        return Pair(optimizer.iterate(population, fitnessValues), info)
    }

    /**
     *
     */
    override fun optimize(): OptimizationResult<T> {
        startTime = System.currentTimeMillis()

        var population = optimizer.initialize()

        do {
            val (p, i) = iterate(population)
            population = p
        } while (!monitor.stop(i))

        return OptimizationResult(
            problem.decode(bestSolution),
            if (goal == GoalType.Maximize) -bestFitness else bestFitness,
            itrCounter,
            System.currentTimeMillis() - startTime,
            evalCounter
        )
    }
}


