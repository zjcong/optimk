package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.Goal
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.IslandEngine
import com.mellonita.optimk.example.benchmarkfuncs.Rastrigin
import com.mellonita.optimk.example.benchmarkfuncs.Sphere
import com.mellonita.optimk.optimizer.BiasedGeneticAlgorithm
import com.mellonita.optimk.optimizer.DifferentialEvolution
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
    val recordInterval = 100
    val maxIteration = 50_000
    val defaultEngineHistory = mutableListOf<Double>()
    val islandEngineHistory = mutableListOf<Double>()
    val problem = Sphere(dimensions)

    val engine = DefaultEngine(
        //optimizer = BiasedGeneticAlgorithm(dimensions = dimensions, population = 150, rng = Random(0)),
        optimizer = DifferentialEvolution(dimensions, 100, DifferentialEvolution.best2(0.3, 0.7), Random(0)),
        goal = Goal.Minimize,
        problem = problem, //rastrigin, zeroOneCounting
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L) {
                defaultEngineHistory.add(it.bestFitness)
            }
            it.bestFitness <= 1E-10 || it.iteration >= maxIteration
        }
    )

    val islandEngine = IslandEngine(
        problem = problem,
        goal = Goal.Minimize,
        migrationInterval = 10,
        optimizers = buildSet {
            (0 until 10).forEach { i ->
                add(
                    if (i.rem(2) == 0)
                        DifferentialEvolution(dimensions, 15, DifferentialEvolution.best1(0.8), Random(i))
                    //BiasedGeneticAlgorithm(dimensions, 15, rng = Random(i))
                    else
                        BiasedGeneticAlgorithm(dimensions, 15, rng = Random(i))
                )
            }
        },
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L)
                islandEngineHistory.add(it.bestFitness)
            it.bestFitness <= 1E-10 || it.iteration >= maxIteration
        }
    )

    val defaultEngineResult = engine.optimize()
    val islandEngineResult = islandEngine.optimize()

    println(defaultEngineResult)
    println(islandEngineResult)

    println("Default, Island")
    defaultEngineHistory.indices.forEach {
        println(defaultEngineHistory[it].toString() + ", " + islandEngineHistory[it])
    }

}