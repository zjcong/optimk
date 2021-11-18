package com.mellonita.optimk.example.benchmarkfuncs

import com.mellonita.optimk.Problem
import com.mellonita.optimk.elementIn

/**
 *
 */
class ExpensiveProblem(d: Int) : Problem<IntArray> {
    override val dimensions: Int = d

    override fun decode(keys: DoubleArray): IntArray = keys.map { it.elementIn(listOf(0, 1)) }.toIntArray()

    override fun objective(candidate: IntArray): Double {
        Thread.sleep(10)
        return candidate.sumOf { it }.toDouble()
    }

    override fun isFeasible(candidate: IntArray): Boolean = true

}

