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

import com.mellonita.optimk.core.engine.AlternatingEngine
import com.mellonita.optimk.core.engine.DefaultEngine
import com.mellonita.optimk.core.sampler.BiasedGeneticAlgorithm
import com.mellonita.optimk.core.sampler.CovarianceMatrixAdaption
import com.mellonita.optimk.core.sampler.DifferentialEvolution
import com.mellonita.optimk.core.sampler.ParticleSwampOptimization
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.example.benchmark.Schwefel
import com.mellonita.optimk.example.benchmark.Sphere
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.markers.SeriesMarkers
import kotlin.random.Random


fun main() {

    val dimensionality = 100
    val population = 30
    val problem = Schwefel(dimensionality)
    val maxIteration = 10_000
    val maxEval = Int.MAX_VALUE
    val alternatingThreshold = 30

    val names = setOf(
        "CMAES",
        "Alternating",
    )

    val engineExperiment = EngineExperiment<DoubleArray>(maxIteration, maxEval, names) { name, monitor ->
        when (name) {
            "CMAES" -> DefaultEngine(
                name = name,
                problem = problem,
                sampler = CovarianceMatrixAdaption(problem.d, population, rng = Random(0)),
                monitor = monitor
            )
            "Alternating" -> AlternatingEngine(
                name = name,
                problem = problem,
                samplers = listOf(
                    CovarianceMatrixAdaption(problem.d, population, Random(0)),
                    DifferentialEvolution(problem.d, population, Random(0)),
                    ParticleSwampOptimization(problem.d, population),
                    BiasedGeneticAlgorithm(problem.d, population),
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
            .title("Default vs Alternating Engine on ${dimensionality}D Schwefel function (p=$population)")
            .xAxisTitle("Iterations")
            .yAxisTitle("Cost")
            .theme(Styler.ChartTheme.GGPlot2)
            .build()

    results.forEach { (name, history) ->
        val series = chart.addSeries(name, history.map { it.second })
        series.marker = SeriesMarkers.NONE
    }

    SwingWrapper(chart)
        .setTitle("OptimK Experiment Result")
        .displayChart()
}