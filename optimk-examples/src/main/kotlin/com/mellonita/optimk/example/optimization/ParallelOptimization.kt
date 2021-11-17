package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.GoalType
import com.mellonita.optimk.engine.*
import com.mellonita.optimk.example.benchmarkfuncs.Rastrigin
import com.mellonita.optimk.example.benchmarkfuncs.ZeroOneCounting
import com.mellonita.optimk.optimizer.BRKGA


fun main() {
    val dimensions = 20

    val rastrigin = Rastrigin(dimensions)
    val zeroOneCounting = ZeroOneCounting(dimensions)


    val engine =
        //ParallelEngine(
        SequentialEngine(
        goal = GoalType.Minimize,
        optimizerClass = BRKGA::class,
        optimizerParameters = mapOf(
            BRKGA.PARAM_POPULATION to 200,
            BRKGA.PARAM_DIMENSIONS to dimensions,
        ),
        problem = zeroOneCounting, //rastrigin, oneCounting
        stop = fun(itr: Long, fitness: Double, eval: Long, start: Long): Boolean {
            return fitness == 0.0
        },
        monitor = { _, fitnessValues ->
            //println(fitnessValues.minOf { it })
        }
    )

    val result = engine.optimize()

    println(result)
}