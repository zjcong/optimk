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

import com.formdev.flatlaf.FlatLightLaf
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.IslandEngine
import com.mellonita.optimk.engine.IslandEngine.Companion.islandsOf
import com.mellonita.optimk.example.benchmark.Benchmark
import com.mellonita.optimk.example.benchmark.Schwefel
import com.mellonita.optimk.optimizer.*
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.random.Random
import kotlin.reflect.full.primaryConstructor


fun experiment(problem: Benchmark, population: Int, maxItr: Int): XYChart? {

    val maxEval = Int.MAX_VALUE

    val names = setOf(
        "PSO",
        "CMAES",
        "DE",
        "GA",
        "Islands",
    )

    val islandNumber = 5

    val engineExperiment = EngineExperiment<DoubleArray>(maxItr, maxEval, names) { name, monitor ->
        when (name) {
            "PSO" -> DefaultEngine(
                problem = problem,
                optimizer = ParticleSwampOptimization(
                    d = problem.d,
                    p = population,
                    rng = Random(System.nanoTime())
                ),
                monitor = monitor
            )
            "CMAES" -> DefaultEngine(
                problem = problem,
                optimizer = CovarianceMatrixAdaption(
                    d = problem.d,
                    p = population,
                    rng = Random(System.nanoTime())

                ),
                monitor = monitor
            )
            "DE" -> DefaultEngine(
                problem = problem,
                optimizer = DifferentialEvolution(
                    d = problem.d,
                    p = population,
                    rng = Random(System.nanoTime())
                ),
                monitor = monitor
            )
            "GA" -> DefaultEngine(
                problem = problem,
                optimizer = BiasedGeneticAlgorithm(
                    d = problem.d,
                    p = population,
                    rng = Random(System.nanoTime())

                ),
                monitor = monitor
            )
            "Islands" -> IslandEngine(
                problem = problem,
                monitor = monitor,
                islands = islandsOf(
                    islandNumber, problem, monitor, listOf(
                        BiasedGeneticAlgorithm(
                            d = problem.d,
                            p = population / islandNumber + 1,
                            rng = Random(System.nanoTime())

                        ),
                        DifferentialEvolution(
                            d = problem.d,
                            p = population / islandNumber + 1,
                            mutation = DifferentialEvolution.best1(0.7),
                            rng = Random(System.nanoTime())

                        ),
                        ParticleSwampOptimization(
                            d = problem.d,
                            p = population / islandNumber + 1,
                            rng = Random(System.nanoTime())

                        ),
                        CovarianceMatrixAdaption(
                            d = problem.d,
                            p = population / islandNumber + 1,
                            rng = Random(System.nanoTime())

                        ),
                    )
                )
            )
            else -> throw IllegalStateException("Unexpected engine $name")
        }
    }

    val results = engineExperiment.start()


    //Fitness Chart
    val fitnessChart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("${problem.d}D ${problem.javaClass.simpleName} (p=$population)")
            .xAxisTitle("Iterations")
            .yAxisTitle("Cost (log axis)")
            .theme(Styler.ChartTheme.Matlab)
            .build()

    results.forEach { (name, history) ->
        val series = fitnessChart.addSeries(
            name,
            history.map { it.first },
            history.map { it.second + 1e-20 })
        series.marker = SeriesMarkers.NONE
    }


    fitnessChart.styler.isYAxisLogarithmic = true

    return fitnessChart
}

fun problemExperiments() {

    val dimensionality = 30
    val population = 60
    val maxItr = 1000
    val problems = Benchmark::class.sealedSubclasses.map { it.primaryConstructor!!.call(dimensionality) }
    val charts = problems.map { experiment(it, population, maxItr) }
    FlatLightLaf.setup() //I like it pretty
    SwingWrapper<XYChart>(charts).displayChartMatrix()
}


fun populationExperiment() {

    val dimensionality = 50
    val maxItr = 4_000
    val charts = (50..450 step 50).map { p ->
        experiment(Schwefel(dimensionality), p, maxItr)
    }

    FlatLightLaf.setup() //I like it pretty
    SwingWrapper<XYChart>(charts).displayChartMatrix()
}

fun main() {
    //populationExperiment()
    problemExperiments()
}