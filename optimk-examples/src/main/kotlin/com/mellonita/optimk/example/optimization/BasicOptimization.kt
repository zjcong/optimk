package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.GoalType
import com.mellonita.optimk.StopCriterion
import com.mellonita.optimk.StopType
import com.mellonita.optimk.engine.BasicEngine
import com.mellonita.optimk.example.benchmarkfuncs.Ackley
import com.mellonita.optimk.example.benchmarkfuncs.ZeroOneCounting
import com.mellonita.optimk.example.benchmarkfuncs.Rastrigin
import com.mellonita.optimk.ga.BRKGA

const val dimensions = 10

val rastrigin = Rastrigin(dimensions)
val ackley = Ackley(dimensions)
val zeroOneCounting = ZeroOneCounting(dimensions)

fun main() {
    val basicEngine = BasicEngine(
        goalType = GoalType.Minimize,
        optimizerClass = BRKGA::class,
        optimizerParams = mapOf(
            BRKGA.PARAM_POPULATION to 1000,
            BRKGA.PARAM_DIMENSIONS to dimensions,
        ),
        problem = rastrigin, //rastrigin, oneCounting
        stoppingCriteria = setOf(
            StopCriterion(StopType.GoalReached, 0.001)
        )
    )

    val result = basicEngine.optimize()

    println(result)
}