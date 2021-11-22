package com.mellonita.optimk.example.optimization

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.mellonita.optimk.Goal
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.IslandEngine
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.optimizer.BiasedGeneticAlgorithm
import com.mellonita.optimk.optimizer.DifferentialEvolution
import com.mellonita.optimk.optimizer.ParticleSwampOptimization
import kotlin.random.Random

fun main() {
    val dimensions = 100
    val recordInterval = 100
    val maxIteration = 100_000
    val defaultEngineHistory = mutableListOf<Double>()
    val islandEngineHistory = mutableListOf<Double>()
    val problem = Rastrigin(dimensions)

    val defaultEngine = DefaultEngine(
        //optimizer = BiasedGeneticAlgorithm(dimensions = dimensions, population = 150, rng = Random(0)),
        optimizer = DifferentialEvolution(dimensions, 300, DifferentialEvolution.best1(0.8), Random(0)),
        goal = Goal.Minimize,
        problem = problem, //rastrigin, zeroOneCounting
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L) {
                defaultEngineHistory.add(it.bestFitness)
            }
            it.iteration >= maxIteration
        }
    )

    val islandEngine = IslandEngine(
        problem = problem,
        goal = Goal.Minimize,
        migrationInterval = 10,
        optimizers = buildSet {
            (0 until 10).forEach { i ->
                add(
                    when (i.rem(3)) {
                        0 -> DifferentialEvolution(dimensions, 30, DifferentialEvolution.best1(0.8), Random(i))
                        1 -> BiasedGeneticAlgorithm(dimensions, 30, rng = Random(i))
                        2 -> ParticleSwampOptimization(dimensions, 30, rng = Random(i))
                        else -> BiasedGeneticAlgorithm(dimensions, 30, rng = Random(i))
                    }
                )
            }
        },
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L)
                islandEngineHistory.add(it.bestFitness)
            it.iteration >= maxIteration
        }
    )

    defaultEngine.optimize()
    islandEngine.optimize()

    csvWriter().open("island_default_history.csv") {
        writeRow("Default", "Island")
        defaultEngineHistory.indices.forEach { i ->
            writeRow(defaultEngineHistory[i], islandEngineHistory[i])
        }
    }

}