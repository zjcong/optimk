package com.mellonita.optimk.example.benchmarkfuncs

import com.mellonita.optimk.Problem
import com.mellonita.optimk.valueIn
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

class Rastrigin(private val dimensions: Int) : Problem<DoubleArray> {
    private val A = 5

    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.map { it.valueIn((-5.12).rangeTo(5.12)) }.toDoubleArray()
    }

    override fun objective(candidate: DoubleArray): Double {
        return A * dimensions + candidate.sumOf { x -> x.pow(2) - A * cos(2 * PI * x) }
    }
}