package com.mellonita.optimk.example.benchmarkfuncs

import com.mellonita.optimk.Problem

/**
 *
 */
class ExpensiveProblem : Problem<DoubleArray> {

    override fun decode(keys: DoubleArray): DoubleArray = keys

    override fun objective(candidate: DoubleArray): Double {
        Thread.sleep(10)
        return candidate.sumOf { it }
    }

    override fun isFeasible(candidate: DoubleArray): Boolean = true

}

