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

@file:Suppress("DuplicatedCode")

package com.mellonita.optimk.coco

import Timing
import com.mellonita.optimk.core.Engine
import com.mellonita.optimk.core.LogLevel
import com.mellonita.optimk.core.engine.AlternatingEngine
import com.mellonita.optimk.core.engine.IslandEngine
import com.mellonita.optimk.core.engine.RestartEngine
import com.mellonita.optimk.core.sampler.BiasedGeneticAlgorithm
import com.mellonita.optimk.core.sampler.CovarianceMatrixAdaption
import com.mellonita.optimk.core.sampler.DifferentialEvolution
import com.mellonita.optimk.core.sampler.ParticleSwampOptimization
import kotlin.random.Random

const val restartMultiplier = 35
const val populationMultiplier = 50
val LOGLEVEL = LogLevel.WARN

/**
 *
 */
@Suppress("unused", "SameParameterValue")
private fun engineOf(
    name: String,
    problem: SingleObjectiveCOCOProblem,
    population: Int,
    seed: Int,
): Engine<DoubleArray> {
    require(population > 10) { "population too small" }
    @Suppress("SpellCheckingInspection")
    return when (name) {
        "CMAES" -> RestartEngine(
            name = name,
            monitor = problem.getMonitor(),
            sampler = CovarianceMatrixAdaption(problem.d, population, Random(seed)),
            problem = problem,
            rng = Random(seed),
            threshold = (problem.d * restartMultiplier)
        )
        "DE" -> RestartEngine(
            name = name,
            monitor = problem.getMonitor(),
            sampler = DifferentialEvolution(problem.d, population, Random(seed)),
            problem = problem,
            rng = Random(seed),
            threshold = (problem.d * restartMultiplier)

        )
        "PSO" -> RestartEngine(
            name = name,
            monitor = problem.getMonitor(),
            sampler = ParticleSwampOptimization(problem.d, population, Random(seed)),
            problem = problem,
            rng = Random(seed),
            threshold = (problem.d * restartMultiplier)

        )
        "GA" -> RestartEngine(
            name = name,
            monitor = problem.getMonitor(),
            sampler = BiasedGeneticAlgorithm(problem.d, population, Random(seed)),
            problem = problem,
            rng = Random(seed),
            threshold = (problem.d * restartMultiplier)

        )
        "ISLAND" -> {
            val restartIslands: List<Engine<DoubleArray>> = listOf(
                CovarianceMatrixAdaption(problem.d, (population / 4), Random(seed)),
                BiasedGeneticAlgorithm(problem.d, (population / 4), Random(seed)),
                ParticleSwampOptimization(problem.d, (population / 4), Random(seed)),
                DifferentialEvolution(problem.d, (population / 4), Random(seed))
            ).map {
                RestartEngine(
                    name = "Island-${it.javaClass.simpleName}",
                    problem = problem,
                    sampler = it,
                    monitor = problem.getMonitor(),
                    threshold = (problem.d * restartMultiplier)
                )
            }

            IslandEngine(
                name = "COCO Island",
                problem = problem,
                monitor = problem.getMonitor(),
                islands = restartIslands,
            )
        }
        "ALTERISLAND" -> {
            val samplers = listOf(
                CovarianceMatrixAdaption(problem.d, (population / 4), Random(seed)),
                BiasedGeneticAlgorithm(problem.d, (population / 4), Random(seed)),
                ParticleSwampOptimization(problem.d, (population / 4), Random(seed)),
                DifferentialEvolution(problem.d, (population / 4), Random(seed))
            )

            val alterIslands = samplers.indices.map {
                AlternatingEngine(
                    name = "AlterIsland-${it.javaClass.simpleName}",
                    problem = problem,
                    samplers = samplers.shuffled(),
                    monitor = problem.getMonitor(),
                    threshold = (problem.d * restartMultiplier)
                )
            }

            IslandEngine(
                name = "COCO AlterIsland",
                problem = problem,
                monitor = problem.getMonitor(),
                islands = alterIslands,
            )
        }
        "ALTER" -> {
            val samplers = listOf(
                CovarianceMatrixAdaption(problem.d, (population / 4), Random(seed)),
                BiasedGeneticAlgorithm(problem.d, (population / 4), Random(seed)),
                ParticleSwampOptimization(problem.d, (population / 4), Random(seed)),
                DifferentialEvolution(problem.d, (population / 4), Random(seed))
            )

            AlternatingEngine(
                name = "AlterEngine",
                problem = problem,
                samplers = samplers.shuffled(),
                monitor = problem.getMonitor(),
                threshold = (problem.d * restartMultiplier)
            )
        }
        else -> throw IllegalArgumentException("Unknown sampler: $name")
    }
}

/**
 *
 */
fun runExperiment(benchmark: COCOBenchmark) {

    var problem: SingleObjectiveCOCOProblem? = benchmark.nextProblem

    val timing = Timing()

    while (problem != null) {

        val population = problem.d * populationMultiplier
        val engine = engineOf(benchmark.algorithmName, problem, population, 0x1980416)

        engine.optimize()

        timing.timeProblem(problem.cocoProblem)
        problem = benchmark.nextProblem
    }

    timing.output()
}

/**
 *
 */
fun main(args: Array<String>) {
    require(args.size == 2)
    require(args[0] in listOf("bbob", "bbob-mixint"))
    require(args[1].uppercase() in listOf("CMAES", "DE", "PSO", "GA", "ISLAND", "ALTERISLAND", "ALTER"))


    val benchmark = when (args[0]) {
        "bbob" -> BBOBBenchmark(
            args[1].uppercase(),
            "OptimK Implementation of ${args[1].uppercase()}",
            "dimensions: 2,3,5,10,20,40",
            5E4.toLong()
        )
        "bbob-mixint" -> BBOBBMixIntBenchmark(
            args[1].uppercase(),
            "OptimK Implementation of ${args[1].uppercase()}",
            "dimensions: 5,10,20,40,80,160",
            5E4.toLong()
        )
        else -> throw IllegalArgumentException()
    }

    runExperiment(benchmark)
}
