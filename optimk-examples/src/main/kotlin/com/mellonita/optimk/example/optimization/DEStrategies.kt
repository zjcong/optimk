package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.Goal
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.example.benchmark.Sphere
import com.mellonita.optimk.optimizer.DifferentialEvolution
import kotlin.random.Random

fun main() {
    val dimensions = 30
    val recordInterval = 10
    val maxIteration = 10_000
    val strategy1History = mutableListOf<Double>()
    val strategy2History = mutableListOf<Double>()


    val strategy1 = DefaultEngine(
        optimizer = DifferentialEvolution(
            dimensions = dimensions,
            population = 60,
            mutation = DifferentialEvolution.best2(0.3, 0.7),
            rng = Random(0)
        ),
        goal = Goal.Minimize,
        problem = Sphere(dimensions), //rastrigin, zeroOneCounting
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L) {
                strategy1History.add(it.bestFitness)
            }
            it.bestFitness <= 10E-8 || it.iteration >= maxIteration
        }
    )


    val strategy2 = DefaultEngine(
        optimizer = DifferentialEvolution(
            dimensions = dimensions,
            population = 60,
            mutation = DifferentialEvolution.currentToBest1(0.3, 0.7),
            rng = Random(0)
        ),
        goal = Goal.Minimize,
        problem = Sphere(dimensions), //rastrigin, zeroOneCounting
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L) {
                strategy2History.add(it.bestFitness)
            }
            it.bestFitness <= 10E-8 || it.iteration >= maxIteration
        }
    )

    val defaultEngineResult = strategy1.optimize()
    val islandEngineResult = strategy2.optimize()

    println(defaultEngineResult)
    println(islandEngineResult)

}