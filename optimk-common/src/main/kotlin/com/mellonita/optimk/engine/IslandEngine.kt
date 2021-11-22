package com.mellonita.optimk.engine

import com.mellonita.optimk.*


/**
 * Island
 */
private class SimpleIsland(
    val optimizer: Optimizer,
    var fitnessValues: DoubleArray,
    var bestFitness: Double,
    var bestSolution: DoubleArray,
    val objectiveFunc: (DoubleArray) -> Double
) {

    var currentGeneration = optimizer.initialize()

    val isOpen = optimizer is OpenBorder

    /**
     *
     */
    fun updateFitness() {
        fitnessValues = currentGeneration.map { objectiveFunc(it) }.toDoubleArray()
        val min = fitnessValues.withIndex().minByOrNull { it.value }!!
        this.bestFitness = min.value
        this.bestSolution = currentGeneration[min.index]
    }

    /**
     *
     */
    fun iterate() {
        currentGeneration = optimizer.iterate(currentGeneration, fitnessValues)
    }

}

/**
 * Island Model Engine
 */
class IslandEngine<T>(
    problem: Problem<T>,
    optimizers: Set<Optimizer>,
    goal: Goal,
    private val migrationInterval: Int,
    monitor: (IterationInfo<T>) -> Boolean
) : Engine<T>(problem, goal, monitor) {

    //Islands
    private val islands: List<SimpleIsland> = optimizers.map {
        SimpleIsland(
            optimizer = it,
            fitnessValues = doubleArrayOf(),
            bestFitness = Double.MAX_VALUE,
            bestSolution = doubleArrayOf(),
            objectiveFunc = ::evaluate
        )
    }

    //Open Island
    private val openIslands = islands.filter { it.isOpen }

    /**
     * Perform optimization
     */
    override fun optimize(): IterationInfo<T> {
        startTime = System.currentTimeMillis()

        var info: IterationInfo<T>

        do {
            itrCounter++

            islands.parallelStream().forEach { it.updateFitness() }

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
                time = System.currentTimeMillis() - startTime
            )

            islands.parallelStream().forEach { it.iterate() }
        } while (!monitor(info))

        return info
    }

    /**
     *
     */
    private fun migrate() {
        if (openIslands.isEmpty()) return
        val from = islands.random()
        val to = openIslands.random()
        val worst = to.fitnessValues.withIndex().maxByOrNull { it.value }!!
        to.currentGeneration[worst.index] = from.bestSolution
        to.fitnessValues[worst.index] = from.bestFitness
    }
}