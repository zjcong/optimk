package com.mellonita.optimk.example.optimization


import com.mellonita.optimk.Goal
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.optimizer.ParticleSwampOptimization
import kotlin.random.Random

fun main() {
    val dimensions = 10
    val recordInterval = 10
    val maxIteration = 10_000
    val history = mutableListOf<Double>()

    val engine = DefaultEngine(
        optimizer = ParticleSwampOptimization(
            dimensions = dimensions,
            population = 100,
            rng = Random(0)
        ),
        goal = Goal.Minimize,
        problem = Rastrigin(dimensions), //rastrigin, zeroOneCounting
        monitor = {
            if (it.iteration.rem(recordInterval) == 0L) {
                history.add(it.bestFitness)
            }
            it.bestFitness <= 10E-8 || it.iteration >= maxIteration
        }
    )


    val defaultEngineResult = engine.optimize()
    println(history)

}