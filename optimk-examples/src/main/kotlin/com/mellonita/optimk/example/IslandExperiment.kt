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
import com.mellonita.optimk.engine.AlternatingEngine
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.IslandEngine
import com.mellonita.optimk.engine.islandsOf
import com.mellonita.optimk.example.benchmark.Ackley
import com.mellonita.optimk.example.benchmark.Michalewicz
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.example.benchmark.Sphere
import com.mellonita.optimk.example.experiment.EngineExperiment
import com.mellonita.optimk.optimizer.BiasedGeneticAlgorithm
import com.mellonita.optimk.optimizer.DifferentialEvolution
import com.mellonita.optimk.optimizer.ParticleSwampOptimization
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.random.Random


fun main() {

    val dimensionality = 6
    val population = 30
    //val problem = Schwefel(dimensionality)
    val problem = Ackley(dimensionality)
    val maxItr = 1000L
    val islandNumber = 3
    val immigrationInterval: Int = 1
    val alternatingThreshold = 20L

    val names = setOf(
        "PSO - Default",
        "DE - Default",
        "GA - Default",
        "GA+PSO+DE Island",
        "GA+PSO+DE Alternating"
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
            "DE - Default" -> DefaultEngine(
                problem = problem,
                goal = Goal.Minimize,
                optimizer = DifferentialEvolution(
                    dimensionality = problem.d,
                    population = population,
                    mutation = DifferentialEvolution.rand1(0.7)
                ),
                monitor = monitor
            )
            "GA - Default" -> DefaultEngine(
                problem = problem,
                goal = Goal.Minimize,
                optimizer = BiasedGeneticAlgorithm(
                    dimensionality = problem.d,
                    population = population,
                ),
                monitor = monitor
            )
            "GA+PSO+DE Island" -> IslandEngine(
                problem = problem,
                goal = Goal.Minimize,
                migrationInterval = immigrationInterval,
                monitor = monitor,
                islands = islandsOf(
                    islandNumber, problem, Goal.Minimize, monitor, listOf(
                        BiasedGeneticAlgorithm(
                            dimensionality = problem.d,
                            population = population / islandNumber + 1,
                            rng = Random(System.currentTimeMillis()),
                        ),
                        DifferentialEvolution(
                            dimensionality = problem.d,
                            population = population,
                            mutation = DifferentialEvolution.rand1(0.7)
                        ),
                        ParticleSwampOptimization(
                            dimensionality = problem.d,
                            population = population,
                            rng = Random(System.currentTimeMillis()),
                        )
                    )
                )
            )
            "GA+PSO+DE Alternating" -> AlternatingEngine(
                problem = problem,
                goal = Goal.Minimize,
                optimizers = listOf(
                    BiasedGeneticAlgorithm(
                        dimensionality = problem.d,
                        population = population
                    ),
                    DifferentialEvolution(
                        dimensionality = problem.d,
                        population = population,
                        mutation = DifferentialEvolution.rand1(0.7)
                    ),
                    ParticleSwampOptimization(
                        dimensionality = problem.d,
                        population = population
                    ),
                ),
                threshold = alternatingThreshold,
                monitor = monitor
            )
            else -> throw IllegalStateException("Unknown engine ")
        }
    }

    val results = engineExperiment.start()

    val chart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("Default vs. Island on ${dimensionality}D ${problem.javaClass.simpleName} function (p=$population)")
            .xAxisTitle("Iterations (x100)")
            .yAxisTitle("Cost (log axis)")
            .theme(Styler.ChartTheme.GGPlot2)
            .build()

    results.forEach { (name, history) ->
        val series = chart.addSeries(
            name,
            history.map { (it) })
        series.marker = SeriesMarkers.NONE
    }

    FlatLightLaf.setup() //I like it pretty
    SwingWrapper(chart)
        .setTitle("OptimK Experiment Result")
        .displayChart()

}