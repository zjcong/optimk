package com.mellonita.optimk.optimizer

import com.mellonita.optimk.Optimizer

class ParticleSwamp(
    val dimension: Int,
    val population: Int
) : Optimizer {
    override fun iterate(currentGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray> {
        TODO("Not yet implemented")
    }

    override fun initialize(): Array<DoubleArray> {
        TODO("Not yet implemented")
    }
}