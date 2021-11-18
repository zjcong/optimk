package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.GOAL_MIN
import com.mellonita.optimk.engine.ParallelEngine
import com.mellonita.optimk.engine.SequentialEngine
import com.mellonita.optimk.example.benchmarkfuncs.ExpensiveProblem
import com.mellonita.optimk.example.benchmarkfuncs.Rastrigin
import com.mellonita.optimk.example.benchmarkfuncs.ZeroOneCounting
import com.mellonita.optimk.optimizer.BRKGA


fun main() {
    val dimensions = 20

    val rastrigin = Rastrigin(dimensions)
    val zeroOneCounting = ZeroOneCounting(dimensions)

    val engine = ParallelEngine(
        optimizer = BRKGA(dimensions = dimensions, population = 10),
        goal = GOAL_MIN,
        problem = rastrigin, //rastrigin, zeroOneCounting
        monitor = { it.iteration >= 100 }
    )

    val result = engine.optimize()

    println(result)
}