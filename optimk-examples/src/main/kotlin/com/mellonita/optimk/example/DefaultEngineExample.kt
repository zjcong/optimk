/*
 * Copyright (C) Zijie Cong 2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mellonita.optimk.example

import com.mellonita.optimk.Engine
import com.mellonita.optimk.LogLevel
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.RestartEngine
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.example.benchmark.Schwefel
import com.mellonita.optimk.example.benchmark.Sphere
import com.mellonita.optimk.example.benchmark.SumOfDifferentPowers
import com.mellonita.optimk.monitor.DefaultMonitor
import com.mellonita.optimk.optimizer.CovarianceMatrixAdaption
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.random.Random


fun main() {
    val d = 50
    val p = 100

    val problem = Schwefel(d)

    val restartHistory = mutableListOf<Double>()
    val defaultHistory = mutableListOf<Double>()

    val restartEngine = RestartEngine(
        problem = problem,
        optimizer = CovarianceMatrixAdaption(d, p),
        //optimizer = BiasedGeneticAlgorithm(d, p, rng = Random(0)),
        monitor = object : DefaultMonitor<DoubleArray>(LogLevel.INFO) {
            override fun stop(engine: Engine<DoubleArray>): Boolean {
                restartHistory.add(engine.bestFitness)
                return engine.iterations >= 1000
            }
        },
        threshold = 20
    )

    val defaultEngine = DefaultEngine(
        problem = problem,
        optimizer = CovarianceMatrixAdaption(d, p),
        //optimizer = BiasedGeneticAlgorithm(d, p, rng = Random(0)),
        monitor = object : DefaultMonitor<DoubleArray>(LogLevel.INFO) {
            override fun stop(engine: Engine<DoubleArray>): Boolean {
                defaultHistory.add(engine.bestFitness)
                return engine.iterations >= 1000
            }
        },
    )

    restartEngine.optimize()
    defaultEngine.optimize()

    val chart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("Restart Engine Example")
            .xAxisTitle("Iterations")
            .yAxisTitle("Cost")
            .theme(Styler.ChartTheme.GGPlot2)
            .build()

    chart.addSeries("Restart", restartHistory).marker = SeriesMarkers.NONE
    chart.addSeries("Default", defaultHistory).marker = SeriesMarkers.NONE

    chart.styler.isYAxisLogarithmic = true

    SwingWrapper(chart).displayChart()

    //history.forEach { println(it) }
}