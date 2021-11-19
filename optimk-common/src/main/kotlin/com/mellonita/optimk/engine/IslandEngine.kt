package com.mellonita.optimk.engine

import com.mellonita.optimk.Engine
import com.mellonita.optimk.IterationInfo
import com.mellonita.optimk.Optimizer

class IslandEngine<T>(
    private val islands: Int,
    private val optimizers: Set<Optimizer>,
    monitor: (IterationInfo<T>) -> Boolean
) : Engine<T>(monitor) {

    override fun optimize(): IterationInfo<T> {
        TODO("Not yet implemented")
    }

    override fun evaluate(candidate: DoubleArray): Double {
        TODO("Not yet implemented")
    }
}