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

import com.mellonita.optimk.core.Engine
import com.mellonita.optimk.core.LogLevel
import com.mellonita.optimk.core.engine.DefaultEngine
import com.mellonita.optimk.core.monitor.DefaultMonitor
import com.mellonita.optimk.core.sampler.BiasedGeneticAlgorithm
import com.mellonita.optimk.core.sampler.CovarianceMatrixAdaption
import com.mellonita.optimk.core.sampler.DifferentialEvolution
import com.mellonita.optimk.example.tsp.ATT48
import com.mellonita.optimk.example.tsp.DANTZIG42
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.random.Random


fun main() {
    val p = 30
    val problem = DANTZIG42()
    val maxItr = 1000
    val defaultHistory = mutableListOf<Double>()


    val defaultEngine = DefaultEngine(
        name = "TSP Example",
        problem = problem,
        sampler = BiasedGeneticAlgorithm(problem.d, p, rng = Random(0)),
        //optimizer = BiasedGeneticAlgorithm(d, p, rng = Random(0)),
        monitor = object : DefaultMonitor<IntArray>(LogLevel.DEBUG) {
            override fun stop(engine: Engine<IntArray>): Boolean {
                defaultHistory.add(engine.bestFitness)
                return engine.iterations >= maxItr
            }
        },
    )

    defaultEngine.optimize()

    val chart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("TSP - Default Engine Example")
            .xAxisTitle("Iterations")
            .yAxisTitle("Distance")
            .theme(Styler.ChartTheme.GGPlot2)
            .build()

    chart.addSeries("Default", defaultHistory).marker = SeriesMarkers.NONE

    //chart.styler.isYAxisLogarithmic = true

    SwingWrapper(chart).displayChart()

    //history.forEach { println(it) }
}