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
import com.mellonita.optimk.core.Engine
import com.mellonita.optimk.core.Monitor
import com.mellonita.optimk.core.Problem
import com.mellonita.optimk.core.Sampler
import com.mellonita.optimk.core.engine.DefaultEngine
import com.mellonita.optimk.core.engine.IslandEngine
import com.mellonita.optimk.core.engine.RestartEngine
import com.mellonita.optimk.core.sampler.BiasedGeneticAlgorithm
import com.mellonita.optimk.core.sampler.CovarianceMatrixAdaption
import com.mellonita.optimk.core.sampler.DifferentialEvolution
import com.mellonita.optimk.core.sampler.ParticleSwampOptimization
import com.mellonita.optimk.example.benchmark.Benchmark
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.example.tsp.DANTZIG42
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.random.Random
import kotlin.reflect.full.primaryConstructor

/**
 *
 */
fun <T> islandsOf(
    n: Int,
    problem: Problem<T>,
    monitor: Monitor<T>,
    samplers: List<Sampler>
): List<Engine<T>> {
    return (0 until n).map {
        RestartEngine(
            name = "Island-${samplers[it.rem(samplers.size)].javaClass.simpleName}",
            problem = problem,
            sampler = samplers[it.rem(samplers.size)],
            //samplers = samplers,
            monitor = monitor,
            threshold = problem.d * 10
        )
    }
}


fun <T> experiment(problem: Problem<T>, population: Int, maxItr: Int, maxEval: Int): XYChart? {

    val names = setOf(
        "PSO",
        "CMAES",
        "DE",
        "GA",
        "Islands",
    )

    val islandNumber = 4

    val engineExperiment = EngineExperiment(maxItr, maxEval, names) { name, monitor: Monitor<T> ->
        when (name) {
            "DE" -> DefaultEngine(
                name = name,
                problem = problem,
                sampler = DifferentialEvolution(
                    problem.d,
                    population,
                    Random(System.nanoTime())
                ),
                monitor = monitor
            )
            "PSO" -> DefaultEngine(
                name = name,
                problem = problem,
                sampler = ParticleSwampOptimization(
                    problem.d,
                    population,
                    Random(System.nanoTime())
                ),
                monitor = monitor
            )
            "CMAES" -> DefaultEngine(
                name = name,
                problem = problem,
                sampler = CovarianceMatrixAdaption(
                    problem.d,
                    population,
                    Random(System.nanoTime())
                ),
                monitor = monitor
            )
            "GA" -> DefaultEngine(
                name = name,
                problem = problem,
                sampler = BiasedGeneticAlgorithm(
                    problem.d,
                    population,
                    Random(System.nanoTime())
                ),
                monitor = monitor
            )
            "Islands" -> IslandEngine(
                name = name,
                problem = problem,
                monitor = monitor,
                islands = islandsOf(
                    islandNumber, problem, monitor, listOf(
                        CovarianceMatrixAdaption(problem.d, population / islandNumber, Random(System.nanoTime())),
                        BiasedGeneticAlgorithm(problem.d, population / islandNumber, Random(System.nanoTime())),
                        ParticleSwampOptimization(problem.d, population / islandNumber, Random(System.nanoTime())),
                        DifferentialEvolution(problem.d, population / islandNumber, Random(System.nanoTime()))
                    )
                ),
            )
            else -> throw IllegalStateException("Unexpected engine $name")
        }
    }

    val results = engineExperiment.start()


    //Fitness Chart
    val convergenceChart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("${problem.d}D ${problem.javaClass.simpleName} (p=$population)")
            .xAxisTitle("Iterations")
            .yAxisTitle("Cost (log axis)")
            .theme(Styler.ChartTheme.Matlab)
            .build()

    results.forEach { (name, history) ->
        val series = convergenceChart.addSeries(
            name,
            history.map { it.first },
            history.map { it.second + 1e-20 })
        series.marker = SeriesMarkers.NONE
    }
    convergenceChart.styler.isYAxisLogarithmic = true
    convergenceChart.styler.xAxisTickMarkSpacingHint = 99
    return convergenceChart
}

/**
 *
 */
fun continuousBenchmarkProblems() {
    val dimensions = 40
    val population = 120
    val maxItr = 5_000
    val maxEval = Int.MAX_VALUE

    val problems = Benchmark::class.sealedSubclasses.map { it.primaryConstructor!!.call(dimensions) }
    val charts = problems.map { experiment(it, population, maxItr, maxEval) }
    FlatLightLaf.setup() //I like it pretty
    SwingWrapper<XYChart>(charts).displayChartMatrix()
}


fun populationExperiment() {
    val dimensions = 10
    val maxItr = 1_000
    val maxEval = Int.MAX_VALUE

    val charts = (50..500 step 50).map { p ->
        experiment(Rastrigin(dimensions), p, maxItr, maxEval)
    }
    FlatLightLaf.setup() //I like it pretty
    SwingWrapper<XYChart>(charts).displayChartMatrix()
}

fun tspWithVariousSamplers() {
    val tsp = DANTZIG42()
    val maxItr = 10_000
    val population = 90
    val maxEval = Int.MAX_VALUE
    val chart = experiment(tsp, population, maxItr, maxEval)

    FlatLightLaf.setup() //I like it pretty
    SwingWrapper<XYChart>(chart).displayChart()
}

fun main() {
    //populationExperiment()
    continuousBenchmarkProblems()
    //tspWithVariousSamplers()
}