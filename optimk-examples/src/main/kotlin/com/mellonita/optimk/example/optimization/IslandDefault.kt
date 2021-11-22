package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.Goal
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.IslandEngine
import com.mellonita.optimk.example.benchmarkfuncs.Ackley
import com.mellonita.optimk.optimizer.BiasedGeneticAlgorithm
import kotlin.random.Random


/*
class Monitor<T> {
    var started: Boolean = false

    var lastUpdateTime = System.currentTimeMillis()
    private val data = Pair(mutableListOf<Long>(), mutableListOf<Double>())

    private var chart: XYChart? = null
    private var sw: SwingWrapper<XYChart>? = null


    fun monitor(iterationInfo: IterationInfo<T>): Boolean {
        if (iterationInfo.iteration.rem(1000) == 0L) {
            lastUpdateTime += iterationInfo.time
            data.first.add(iterationInfo.evaluation)
            data.second.add(iterationInfo.bestFitness)

            if (started) {
                val evaluations = data.first


                val fitness = data.second

                chart!!.updateXYSeries("Best Fitness", evaluations, fitness, null)
                sw!!.repaintChart()

            } else {
                chart = QuickChart.getChart(
                    "Monitor",
                    "Evaluations",
                    "Fitness",
                    "Best Fitness",
                    data.first,
                    data.second
                )
                sw = SwingWrapper(chart)
                sw!!.displayChart()
                started = true
            }
        }
        return iterationInfo.iteration >= 100_000
    }

}
*/

fun main() {
    val dimensions = 50
    val recordInterval = 1000
    val maxIteration = 1_000_000
    val defaultEngineHistory = mutableListOf<Double>()
    val islandEngineHistory = mutableListOf<Double>()

    val engine = DefaultEngine(
        optimizer = BiasedGeneticAlgorithm(dimensions = dimensions, population = 150, rng = Random(0)),
        //optimizer = DifferentialEvolution(dimensions, 100, DifferentialEvolution.rand1(0.8), Random(0)),
        goal = Goal.Minimize,
        problem = Ackley(dimensions), //rastrigin, zeroOneCounting
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L) {
                defaultEngineHistory.add(it.bestFitness)
            }
            it.bestFitness <= 1E-7 || it.iteration >= maxIteration
        }
    )

    val islandEngine = IslandEngine(
        problem = Ackley(dimensions),
        goal = Goal.Minimize,
        migrationInterval = 5,
        optimizers = buildSet {
            (0 until 10).forEach { i ->
                add(
                    if (i.rem(2) == 0)
                    //DifferentialEvolution(dimensions, 15, DifferentialEvolution.rand1(0.9), Random(i))
                        BiasedGeneticAlgorithm(dimensions, 15)
                    else
                        BiasedGeneticAlgorithm(dimensions, 15)
                )
            }
        },
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L)
                islandEngineHistory.add(it.bestFitness)
            it.bestFitness <= 1E-7 || it.iteration >= maxIteration
        }
    )

    val defaultEngineResult = engine.optimize()
    val islandEngineResult = islandEngine.optimize()

    println(defaultEngineResult)
    println(islandEngineResult)

}