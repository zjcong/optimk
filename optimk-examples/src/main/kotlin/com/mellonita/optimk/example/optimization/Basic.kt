package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.GoalType
import com.mellonita.optimk.engine.SequentialEngine
import com.mellonita.optimk.example.benchmarkfuncs.Rastrigin
import com.mellonita.optimk.example.benchmarkfuncs.ZeroOneCounting
import com.mellonita.optimk.optimizer.BRKGA


fun main() {
    val dimensions = 20

    val rastrigin = Rastrigin(dimensions)
    val zeroOneCounting = ZeroOneCounting(dimensions)

    val engine = SequentialEngine(
        optimizer = BRKGA(dimensions = dimensions, population = 1000),
        goal = GoalType.Minimize,
        problem = rastrigin, //rastrigin, zeroOneCounting
        stop = { info ->
            info.iteration >= 1000
        },
        monitor = { info, _, _ ->
            println(info.bestFitness)
        }
    )

    val result = engine.optimize()

    println(result)
}