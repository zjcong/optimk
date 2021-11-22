package com.mellonita.optimk.engine

import com.mellonita.optimk.*


/**
 * Basic optimization engine
 * @param problem Problem to solve
 * @param goal Goal type, GOAL_MIN or GOAL_MAX
 * @param optimizer Optimizer
 * @param monitor
 */
class DefaultEngine<T>(
    problem: Problem<T>,
    private val optimizer: Optimizer,
    goal: Goal,
    monitor: (info: IterationInfo<T>) -> Boolean
) : Engine<T>(problem, goal, monitor) {


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
                time = System.currentTimeMillis() - startTime
            )

            currentGeneration = optimizer.iterate(currentGeneration, fitnessValues)

        } while (!monitor(info))

        return info
    }
}


