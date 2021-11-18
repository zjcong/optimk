package com.mellonita.optimk.example.benchmarkfuncs

import com.mellonita.optimk.Problem
import com.mellonita.optimk.elementIn

class ZeroOneCounting(d: Int) : Problem<IntArray> {

    override fun decode(keys: DoubleArray): IntArray = keys.map { it.elementIn(listOf(0, 1)) }.toIntArray()

    override fun objective(candidate: IntArray): Double = candidate.sumOf { it }.toDouble()

    override fun isFeasible(candidate: IntArray): Boolean = candidate[0] == 0

}

