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

package com.mellonita.optimk.coco

import Benchmark
import Observer
import Problem
import Suite
import com.mellonita.optimk.core.Engine
import com.mellonita.optimk.core.Goal
import com.mellonita.optimk.core.LogLevel
import com.mellonita.optimk.core.Monitor
import com.mellonita.optimk.core.engine.DefaultEngine
import com.mellonita.optimk.core.engine.IslandEngine
import com.mellonita.optimk.core.math.valueIn
import com.mellonita.optimk.core.monitor.DefaultMonitor
import com.mellonita.optimk.core.sampler.BiasedGeneticAlgorithm
import com.mellonita.optimk.core.sampler.CovarianceMatrixAdaption
import com.mellonita.optimk.core.sampler.DifferentialEvolution
import com.mellonita.optimk.core.sampler.ParticleSwampOptimization
import kotlin.random.Random

const val BudgetMultiplier: Int = 1_000
const val IslandPopulationMultiplier = 3
const val maxItr = 8000

/**
 * Convert COCO Problem into OptimK problem
 */
fun Problem.toActualProblem(): com.mellonita.optimk.core.Problem<DoubleArray> {
    val dimension: Int = dimension
    val lvi = largestValuesOfInterest
    val svi = smallestValuesOfInterest
    fun evalFun(x: DoubleArray): Double {
        return this.evaluateFunction(x)[0]
    }
    return object : com.mellonita.optimk.core.Problem<DoubleArray> {
        override val d: Int = dimension
        override val goal: Goal = Goal.Minimize
        override fun objective(solution: DoubleArray): Double {
            return evalFun(solution)
        }

        override fun decode(keys: DoubleArray): DoubleArray {
            return keys.indices.map { i -> keys[i].valueIn(svi[i].rangeTo(lvi[i])) }.toDoubleArray()
        }

    }
}


fun <T> getIslandEngine(
    problem: com.mellonita.optimk.core.Problem<T>,
    monitor: Monitor<T>,
    seed: Int
): IslandEngine<T> {
    val p = IslandPopulationMultiplier * problem.d
    return IslandEngine(
        name = "COCO Island",
        problem = problem,
        monitor = monitor,
        islands = IslandEngine.islandsOf(
            4, problem, monitor, listOf(
                BiasedGeneticAlgorithm(problem.d, p, Random(seed)),
                DifferentialEvolution(problem.d, p, Random(seed)),
                ParticleSwampOptimization(problem.d, p, Random(seed)),
                CovarianceMatrixAdaption(problem.d, p, Random(seed)),
            )
        )
    )
}

fun <T> getDefaultEngine(
    problem: com.mellonita.optimk.core.Problem<T>,
    monitor: Monitor<T>,
    seed: Int,
    samplerName: String
): DefaultEngine<T> {
    val p = IslandPopulationMultiplier * problem.d * 4

    val sampler = when (samplerName) {
        "CMAES" -> CovarianceMatrixAdaption(problem.d, p, Random(0))
        "PSO" -> ParticleSwampOptimization(problem.d, p, Random(0))
        "DE" -> DifferentialEvolution(problem.d, p, Random(0))
        "GA" -> BiasedGeneticAlgorithm(problem.d, p, Random(0))
        else -> throw IllegalArgumentException("$samplerName is not recognized")
    }

    return DefaultEngine(
        name = "Default - CMAES",
        problem = problem,
        monitor = monitor,
        sampler = CovarianceMatrixAdaption(problem.d, p, Random(seed))
    )
}

fun islandExperiment(suiteName: String, observerName: String, seed: Int) {

    val observerOptions = ("result_folder: OptimK_Island_on_" + suiteName + " "
            + "algorithm_name: OptimK_Island "
            + "algorithm_info: \"Multi algorithm islands with CMAES, BRKGA, PSO and DE\"")

    val suite = Suite(suiteName, "", "")
    val observer = Observer(observerName, observerOptions)
    val benchmark = Benchmark(suite, observer)

    var problem: Problem? = benchmark.nextProblem
    while (problem != null) {
        val actualProblem = problem.toActualProblem()

        val monitor = object : DefaultMonitor<DoubleArray>(LogLevel.ERROR) {
            override fun stop(engine: Engine<DoubleArray>): Boolean {
                return (engine.iterations >= maxItr || engine.bestFitness <= 1e-8)
            }
        }

        val engine = getIslandEngine(actualProblem, monitor, seed)
        engine.optimize()
        problem = benchmark.nextProblem
    }

    benchmark.finalizeBenchmark()
}

/**
 *
 */
fun singleAlgorithmExperiment(suiteName: String, observerName: String, seed: Int, samplerName: String) {

    val observerOptions = ("result_folder: OptimK_${samplerName}_on_" + suiteName + " "
            + "algorithm_name: OptimK_${samplerName} "
            + "algorithm_info: \"Original ${samplerName}\"")

    val suite = Suite(suiteName, "", "")
    val observer = Observer(observerName, observerOptions)
    val benchmark = Benchmark(suite, observer)

    var problem: Problem? = benchmark.nextProblem
    while (problem != null) {
        val actualProblem = problem.toActualProblem()

        val monitor = object : DefaultMonitor<DoubleArray>(LogLevel.ERROR) {
            override fun stop(engine: Engine<DoubleArray>): Boolean {
                return (engine.iterations >= maxItr || engine.bestFitness <= 1e-8)
            }
        }

        val engine = getDefaultEngine(actualProblem, monitor, seed, samplerName)
        engine.optimize()
        problem = benchmark.nextProblem
    }

    benchmark.finalizeBenchmark()
}

fun main() {
    singleAlgorithmExperiment("bbob", "bbob", 0, "PSO")
    singleAlgorithmExperiment("bbob", "bbob", 0, "DE")
    singleAlgorithmExperiment("bbob", "bbob", 0, "GA")
    singleAlgorithmExperiment("bbob", "bbob", 0, "CMAES")
    islandExperiment("bbob", "bbob", 0)
}