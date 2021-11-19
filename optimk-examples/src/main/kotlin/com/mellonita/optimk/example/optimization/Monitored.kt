package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.GOAL_MIN
import com.mellonita.optimk.engine.ParallelEngine
import com.mellonita.optimk.example.benchmarkfuncs.Rastrigin
import com.mellonita.optimk.example.benchmarkfuncs.ZeroOneCounting
import com.mellonita.optimk.optimizer.BRKGA


fun main() {
    val dimensions = 5

    val rastrigin = Rastrigin(dimensions)
    // val zeroOneCounting = ZeroOneCounting()

    val engine = ParallelEngine(
        optimizer = BRKGA(dimensions = dimensions, population = 20),
        goal = GOAL_MIN,
        problem = rastrigin, //rastrigin, zeroOneCounting
        monitor = { it.iteration >= 1000 }
    )

    val result = engine.optimize()

    println(result)
}