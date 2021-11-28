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
import com.mellonita.optimk.optimizer.BiasedGeneticAlgorithm
import com.mellonita.optimk.optimizer.CovarianceMatrixAdaption
import com.mellonita.optimk.optimizer.DifferentialEvolution
import com.mellonita.optimk.optimizer.ParticleSwampOptimization
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.reflect.full.primaryConstructor


fun experiment(problem: Benchmark, population: Int, maxItr: Int): XYChart? {

    val maxEval = Int.MAX_VALUE

    val islandNumber = 4

    val names = setOf(
        "PSO",
        "CMAES",
        "DE",
        "GA",
        "Islands",
    )


    val engineExperiment = EngineExperiment<DoubleArray>(maxItr, maxEval, names) { name, monitor ->
        when (name) {
            "PSO" -> DefaultEngine(
                problem = problem,
                optimizer = ParticleSwampOptimization(
                    dimensionality = problem.dimensions,
                    population = population
                ),
                monitor = monitor
            )
            "CMAES" -> DefaultEngine(
                problem = problem,
                optimizer = CovarianceMatrixAdaption(
                    dimensionality = problem.dimensions,
                    population = population,
                ),
                monitor = monitor
            )
            "DE" -> DefaultEngine(
                problem = problem,
                optimizer = DifferentialEvolution(
                    dimensionality = problem.dimensions,
                    population = population,
                    mutation = DifferentialEvolution.best1(0.7)
                ),
                monitor = monitor
            )
            "GA" -> DefaultEngine(
                problem = problem,
                optimizer = BiasedGeneticAlgorithm(
                    dimensionality = problem.dimensions,
                    population = population,
                ),
                monitor = monitor
            )
            "Islands" -> IslandEngine(
                problem = problem,
                monitor = monitor,
                islands = islandsOf(
                    islandNumber, problem, monitor, listOf(
                        BiasedGeneticAlgorithm(
                            dimensionality = problem.dimensions,
                            population = population / islandNumber + 1
                        ),
                        DifferentialEvolution(
                            dimensionality = problem.dimensions,
                            population = population / islandNumber + 1,
                            mutation = DifferentialEvolution.best1(0.7)
                        ),
                        ParticleSwampOptimization(
                            dimensionality = problem.dimensions,
                            population = population / islandNumber + 1,
                        ),
                        CovarianceMatrixAdaption(
                            dimensionality = problem.dimensions,
                            population = population / islandNumber + 1,
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
            .title("${problem.dimensions}D ${problem.javaClass.simpleName} (p=$population)")
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

    val dimensionality = 50
    val population = 100
    val maxItr = 3_000
    val problems = Benchmark::class.sealedSubclasses.map { it.primaryConstructor!!.call(dimensionality) }
    val charts = problems.map { experiment(it, population, maxItr) }
    FlatLightLaf.setup() //I like it pretty
    SwingWrapper<XYChart>(charts).displayChartMatrix()
}


fun populationExperiment() {

    val dimensionality = 50
    val maxItr = 3_000
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