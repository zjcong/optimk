package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.engine.GoalType
import com.mellonita.optimk.engine.StopCriterion
import com.mellonita.optimk.engine.StopType
import com.mellonita.optimk.engine.LoggedEngine
import com.mellonita.optimk.example.benchmarkfuncs.Rastrigin
import com.mellonita.optimk.optimizer.BRKGA


fun main() {
    val dimensions = 10

    val rastrigin = Rastrigin(dimensions)

    val loggedEngine = LoggedEngine(
        goalType = GoalType.Maximize,
        optimizerClass = BRKGA::class,
        optimizerParams = mapOf(
            BRKGA.PARAM_POPULATION to 10,
            BRKGA.PARAM_DIMENSIONS to dimensions,
        ),
        problem = rastrigin, //rastrigin, oneCounting
        stoppingCriteria = setOf(
            StopCriterion(StopType.ItrReached, 1000)
        ),
        logInterval = 10,
        logger = {
            println(it.globalBestFitness)
        }
    )

    val result = loggedEngine.optimize()

    println(result)
}