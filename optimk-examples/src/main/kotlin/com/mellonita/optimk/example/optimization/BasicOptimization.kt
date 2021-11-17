package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.engine.GoalType
import com.mellonita.optimk.engine.StopCriterion
import com.mellonita.optimk.engine.StopType
import com.mellonita.optimk.engine.BasicEngine
import com.mellonita.optimk.example.benchmarkfuncs.Rastrigin
import com.mellonita.optimk.optimizer.BRKGA


fun main() {
    val dimensions = 10

    val rastrigin = Rastrigin(dimensions)


    val basicEngine = BasicEngine(
        goalType = GoalType.Minimize,
        optimizerClass = BRKGA::class,
        optimizerParams = mapOf(
            BRKGA.PARAM_POPULATION to 300,
            BRKGA.PARAM_DIMENSIONS to dimensions,
        ),
        problem = rastrigin, //rastrigin, oneCounting
        stoppingCriteria = setOf(
            StopCriterion(StopType.ItrReached, 1000)
        )
    )

    val result = basicEngine.optimize()

    println(result)
}