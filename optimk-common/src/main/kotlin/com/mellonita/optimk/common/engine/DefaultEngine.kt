package com.mellonita.optimk.common.engine

import com.mellonita.optimk.common.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Default engine
 */
open class DefaultEngine<T>(
    problem: Problem<T>,
    stoppingCriteria: Set<StopCriterion>,
    goalType: GoalType,
    optimizerClass: KClass<out Optimizer>,
    params: Map<String, Any>
) : Engine<T>(problem, stoppingCriteria, goalType, optimizerClass, params) {

    private val optimizer: Optimizer

    init {
        optimizer = optimizerClass.primaryConstructor!!.call(params)
    }

    /**
     *
     */
    override fun optimize(): OptimizationResult<T> {
        startTime = System.currentTimeMillis()


        TODO("Not yet implemented")
    }

}