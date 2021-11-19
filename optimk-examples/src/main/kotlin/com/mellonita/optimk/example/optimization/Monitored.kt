package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.GOAL_MAX
import com.mellonita.optimk.IterationInfo
import com.mellonita.optimk.engine.SequentialEngine
import com.mellonita.optimk.example.benchmarkfuncs.Ackley
import com.mellonita.optimk.optimizer.BRKGA
import org.knowm.xchart.QuickChart
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart


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
        return iterationInfo.iteration >= 1_000_000
    }

}


fun main() {
    val dimensions = 100

    val monitor = Monitor<DoubleArray>()

    val engine = SequentialEngine(
        optimizer = BRKGA(dimensions = dimensions, population = 100),
        goal = GOAL_MAX,
        problem = Ackley(dimensions), //rastrigin, zeroOneCounting
        monitor = monitor::monitor
    )

    val result = engine.optimize()

    println(result)
}