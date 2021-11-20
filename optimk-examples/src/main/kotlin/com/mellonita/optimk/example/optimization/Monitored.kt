package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.Goal
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.IslandEngine
import com.mellonita.optimk.example.benchmarkfuncs.Ackley
import com.mellonita.optimk.optimizer.BRKGA
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.markers.SeriesMarkers
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
    val dimensions = 5
    val stopIteration = 100_000
    val recordInterval = 5_000
    val defaultEngineHistory = mutableListOf<Double>()
    val islandEngineHistory = mutableListOf<Double>()

    val engine = DefaultEngine(
        optimizer = BRKGA(dimensions = dimensions, population = 200, rng = Random(System.currentTimeMillis())),
        goal = Goal.Minimize,
        problem = Ackley(dimensions), //rastrigin, zeroOneCounting
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L)
                defaultEngineHistory.add(it.bestFitness)
            it.iteration >= stopIteration
        }
    )

    val islandEngine = IslandEngine(
        problem = Ackley(dimensions),
        goal = Goal.Minimize,
        migrationInterval = 100,
        optimizers = buildSet {
            (0 until 4).forEach { i ->
                add(BRKGA(dimensions = dimensions, population = 50, rng = Random(i)))
            }
        },
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L)
                islandEngineHistory.add(it.bestFitness)
            it.iteration >= stopIteration
        }
    )

    val defaultEngineResult = engine.optimize()
    val islandEngineResult = islandEngine.optimize()

    val chart = XYChartBuilder()
        .width(1024)
        .height(800)
        .title("Default vs Island Engine")
        .xAxisTitle("X")
        .yAxisTitle("Y")
        .build()

    chart.addSeries("Default", defaultEngineHistory)
    chart.addSeries("Island", islandEngineHistory)

    chart.seriesMap.forEach { it.value.marker = SeriesMarkers.NONE }

    println(defaultEngineResult)
    println(islandEngineResult)
    SwingWrapper(chart).displayChart();
}