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
import Timing
import com.mellonita.optimk.core.Engine
import com.mellonita.optimk.core.Monitor
import com.mellonita.optimk.core.engine.DefaultEngine
import com.mellonita.optimk.core.engine.IslandEngine
import com.mellonita.optimk.core.engine.RestartEngine
import com.mellonita.optimk.core.sampler.BiasedGeneticAlgorithm
import com.mellonita.optimk.core.sampler.CovarianceMatrixAdaption
import com.mellonita.optimk.core.sampler.DifferentialEvolution
import com.mellonita.optimk.core.sampler.ParticleSwampOptimization
import kotlin.random.Random

const val restartMultiplier = 30
const val islandNumber = 4

/**
 *
 */
fun <T> islandsOf(
    n: Int,
    problem: com.mellonita.optimk.core.Problem<T>,
    monitor: Monitor<T>,
    islandPopulation: Int,
    seed: Int
): List<Engine<T>> {

    val samplers = listOf(
        CovarianceMatrixAdaption(problem.d, islandPopulation, Random(seed)),
        BiasedGeneticAlgorithm(problem.d, islandPopulation, Random(seed)),
        ParticleSwampOptimization(problem.d, islandPopulation, Random(seed)),
        DifferentialEvolution(problem.d, islandPopulation/2, Random(seed))
    )

    val restartIsland: List<Engine<T>> = (0 until n).map {
        RestartEngine(
            name = "Island-${samplers[it.rem(samplers.size)].javaClass.simpleName}",
            problem = problem,
            sampler = samplers[it.rem(samplers.size)],
            //samplers = samplers,
            monitor = monitor,
            threshold = (problem.d * restartMultiplier)
        )
    }
    return restartIsland
}

/**
 *
 */
@Suppress("SameParameterValue")
private fun engineOf(
    name: String,
    problem: SingleObjCOCOProblem,
    p: Int,
    seed: Int,
): DefaultEngine<DoubleArray> {
    return when (name) {
        "CMAES" -> RestartEngine(
            name = name,
            monitor = problem.getMonitor(),
            sampler = CovarianceMatrixAdaption(problem.d, p, Random(seed)),
            problem = problem,
            rng = Random(seed),
            threshold = (problem.d * restartMultiplier)
        )
        "DE" -> RestartEngine(
            name = name,
            monitor = problem.getMonitor(),
            sampler = DifferentialEvolution(problem.d, p / 2, Random(seed)),
            problem = problem,
            rng = Random(seed),
            threshold = (problem.d * restartMultiplier)

        )
        "PSO" -> RestartEngine(
            name = name,
            monitor = problem.getMonitor(),
            sampler = ParticleSwampOptimization(problem.d, p, Random(seed)),
            problem = problem,
            rng = Random(seed),
            threshold = (problem.d * restartMultiplier)

        )
        "GA" -> RestartEngine(
            name = name,
            monitor = problem.getMonitor(),
            sampler = BiasedGeneticAlgorithm(problem.d, p, Random(seed)),
            problem = problem,
            rng = Random(seed),
            threshold = (problem.d * restartMultiplier)

        )
        else -> throw IllegalArgumentException("Unknown sampler $name")
    }
}


/**
 *
 */
fun islandExperiment(suiteName: String, observerName: String) {

    val observerOptions = ("result_folder: OptimKIsland_on_" + suiteName + " "
            + "algorithm_name: OptimKIsland "
            + "algorithm_info: \"Multi algorithm islands with CMAES, BRKGA, PSO and DE\"")

    val suite = Suite(suiteName, "", "")
    val observer = Observer(observerName, observerOptions)
    val benchmark = Benchmark(suite, observer)
    var problem: Problem? = benchmark.nextProblem

    while (problem != null) {
        /* Initialize timing */
        val timing = Timing()

        val actualProblem = SingleObjCOCOProblem(problem)

        val islandPopulation = (actualProblem.d * 8)
        val engine = IslandEngine(
            name = "COCO Island",
            problem = actualProblem,
            monitor = actualProblem.getMonitor(),
            islands = islandsOf(islandNumber, actualProblem, actualProblem.getMonitor(), islandPopulation, 0),
        )
        engine.optimize()
        timing.timeProblem(problem)
        problem = benchmark.nextProblem
    }
    benchmark.finalizeBenchmark()
}


/**
 *
 */
fun singleSamplerExperiment(suiteName: String, observerName: String, samplerName: String) {

    val observerOptions = ("result_folder: OptimK${samplerName}_on_" + suiteName + " "
            + "algorithm_name: OptimK${samplerName} "
            + "algorithm_info: \"OptimK${samplerName}\"")

    val suite = Suite(suiteName, "", "")
    val observer = Observer(observerName, observerOptions)
    val benchmark = Benchmark(suite, observer)
    var problem: Problem? = benchmark.nextProblem

    while (problem != null) {

        /* Initialize timing */
        val timing = Timing()
        val actualProblem = SingleObjCOCOProblem(problem)
        val population = islandNumber * actualProblem.d * 8
        val engine = engineOf(samplerName, actualProblem, population, 0)
        engine.optimize()


        /* Keep track of time */
        timing.timeProblem(problem)

        problem = benchmark.nextProblem
    }
    benchmark.finalizeBenchmark()
}

/**
 *
 */
fun main(args: Array<String>) {

    require(args[0] in setOf<String>("CMAES", "PSO", "DE", "GA", "Island"))
    println("${args[0]}=============================\r\n\r\n")
    val name = args[0]



    if (name == "Island") {
        islandExperiment("bbob", "bbob")
        return
    } else
        singleSamplerExperiment("bbob", "bbob", name)
}