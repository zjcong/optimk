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

package com.mellonita.optimk.example.experiment

import com.mellonita.optimk.engine.AlternatingEngine
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.Goal
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.optimizer.DifferentialEvolution
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers


fun main() {

    val dimensionality = 30
    val population = 10
    val problem = Rastrigin(dimensionality)
    val maxIteration = 5_000L
    val alternatingThreshold = 100L

    val names = setOf(
        "DE - CR = 0.8",
        "DE - CR = 0.2",
        "Alternating",
    )

    val engineExperiment = EngineExperiment<DoubleArray>(maxIteration, names) { name, monitor ->
        when (name) {
            "DE - CR = 0.2" -> DefaultEngine(
                problem = problem,
                goal = Goal.Minimize,
                optimizer = DifferentialEvolution(
                    d = problem.d,
                    p = population,
                    cr = 0.2,
                    mutation = DifferentialEvolution.best1(0.8)
                ),
                monitor = monitor
            )
            "DE - CR = 0.8" -> DefaultEngine(
                problem = problem,
                goal = Goal.Minimize,
                optimizer = DifferentialEvolution(
                    d = problem.d,
                    p = population,
                    cr = 0.8,
                    mutation = DifferentialEvolution.best1(0.8)
                ),
                monitor = monitor
            )
            "Alternating" -> AlternatingEngine(
                problem = problem,
                goal = Goal.Minimize,
                optimizers = listOf(
                    DifferentialEvolution(
                        d = problem.d,
                        p = population,
                        cr = 0.2,
                        mutation = DifferentialEvolution.best1(0.8)
                    ),
                    DifferentialEvolution(
                        d = problem.d,
                        p = population,
                        cr = 0.8,
                        mutation = DifferentialEvolution.best1(0.8)
                    )
                ),
                threshold = alternatingThreshold,
                monitor = monitor
            )
            else -> throw IllegalStateException("Should not happen")
        }
    }

    val results = engineExperiment.start()

    val chart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("Default vs Alternating Engine on 30D Rastrigin function (p=60)")
            .xAxisTitle("Iterations")
            .yAxisTitle("Cost")
            .theme(Styler.ChartTheme.GGPlot2)
            .build()

    results.forEach { (name, history) ->
        val series = chart.addSeries(name, history)
        series.marker = SeriesMarkers.NONE
    }

    SwingWrapper(chart)
        .setTitle("OptimK Experiment Result")
        .displayChart()

}