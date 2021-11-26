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

import com.formdev.flatlaf.FlatLightLaf
import com.mellonita.optimk.Goal
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.IslandEngine
import com.mellonita.optimk.example.benchmark.Sphere
import com.mellonita.optimk.example.experiment.EngineExperiment
import com.mellonita.optimk.optimizer.BiasedGeneticAlgorithm
import com.mellonita.optimk.optimizer.ParticleSwampOptimization
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.random.Random


fun main() {

    val dimensionality = 400
    val population = 400
    val problem = Sphere(dimensionality)
    //val problem = Ackley(dimensionality)
    val maxItr = 1_000L
    val islandNumber = 5

    val names = setOf(
        "PSO - Default",
        "GA - Default",
        "PSO+GA Island",
    )

    val engineExperiment = EngineExperiment<DoubleArray>(maxItr, names) { name, monitor ->
        when (name) {
            "PSO - Default" -> DefaultEngine(
                problem = problem,
                goal = Goal.Minimize,
                optimizer = ParticleSwampOptimization(
                    dimensionality = problem.d,
                    population = population
                ),
                monitor = monitor
            )
            "GA - Default"-> DefaultEngine(
                problem = problem,
                goal = Goal.Minimize,
                optimizer = BiasedGeneticAlgorithm(
                    dimensionality = problem.d,
                    population = population
                ),
                monitor = monitor
            )
            "PSO+GA Island" -> IslandEngine(
                problem = problem,
                goal = Goal.Minimize,
                migrationInterval = 1,
                monitor = monitor,
                islands = (0 until islandNumber).map {
                    when (it.rem(2)) {
                        0 -> DefaultEngine(
                            problem = problem,
                            goal = Goal.Minimize,
                            monitor = monitor,
                            optimizer = ParticleSwampOptimization(
                                dimensionality = problem.d,
                                population = population / islandNumber + 1,
                                rng = Random(it),
                            )
                        )
                        else -> DefaultEngine(
                            problem = problem,
                            goal = Goal.Minimize,
                            monitor = monitor,
                            optimizer = BiasedGeneticAlgorithm(
                                dimensionality = problem.d,
                                population = population / islandNumber + 1,
                                rng = Random(it)
                            )
                        )
                    }
                }
            )
            else -> throw IllegalStateException("Unknown engine ")
        }
    }

    val results = engineExperiment.start()

    val chart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("Default PSO vs PSO+GA Island on 30D Ackley function (p=100)")
            .xAxisTitle("Iterations")
            .yAxisTitle("Cost")
            .theme(Styler.ChartTheme.GGPlot2)
            .build()

    results.forEach { (name, history) ->
        val series = chart.addSeries(name, history.subList(1, history.size).map { (it) })
        series.marker = SeriesMarkers.NONE
    }

    FlatLightLaf.setup() //I like it pretty
    SwingWrapper(chart)
        .setTitle("OptimK Experiment Result")
        .displayChart()

}