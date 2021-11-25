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

import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.Goal
import com.mellonita.optimk.engine.IslandEngine
import com.mellonita.optimk.example.benchmark.Ackley
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.optimizer.BiasedGeneticAlgorithm
import com.mellonita.optimk.optimizer.ParticleSwampOptimization
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.random.Random


fun main() {

    val dimensionality = 40
    val population = 100
    val problem = Rastrigin(dimensionality)
    //val problem = Ackley(dimensionality)
    val maxItr = 3_000L
    val islandNumber = 5

    val names = setOf(
        "PSO - Default",
        "Island",
    )

    val experiment = Experiment<DoubleArray>(maxItr, names) { name, monitor ->
        when (name) {
            "PSO - Default" -> DefaultEngine(
                problem = problem,
                goal = Goal.Minimize,
                optimizer = BiasedGeneticAlgorithm(
                    d = problem.d,
                    p = population,
                    //c1 = 2.0,
                    //c2 = 2.0,
                    rng = Random(0),
                ),
                monitor = monitor
            )
            "Island" -> IslandEngine(
                problem = problem,
                goal = Goal.Minimize,
                migrationInterval = 1,
                rng = Random(0),
                monitor = monitor,
                islands = (0 until islandNumber).map {
                    when (it.rem(2)) {
                        0 -> DefaultEngine(
                            problem = problem,
                            goal = Goal.Minimize,
                            monitor = monitor,
                            optimizer = ParticleSwampOptimization(
                                d = problem.d,
                                p = population / islandNumber + 1,
                                c1 = 2.0,
                                c2 = 2.0,
                                rng = Random(it),
                            )
                        )
                        else -> DefaultEngine(
                            problem = problem,
                            goal = Goal.Minimize,
                            monitor = monitor,
                            optimizer = BiasedGeneticAlgorithm(
                                d = problem.d,
                                p = population / islandNumber + 1,
                                rng = Random(it)
                            )
                        )
                    }
                }
            )
            else -> throw IllegalStateException("Unknown engine ")
        }
    }

    val results = experiment.start()

    val chart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("Default PSO vs PSO+GA Island on 40D Ackley function")
            .xAxisTitle("Iterations")
            .yAxisTitle("Cost")
            .theme(Styler.ChartTheme.GGPlot2)
            .build()

    results.forEach { (name, history) ->
        val series = chart.addSeries(name, history.subList(1, history.size).map { (it) })
        series.marker = SeriesMarkers.NONE
    }

    SwingWrapper(chart).displayChart()

}