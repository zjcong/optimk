package com.mellonita.optimk.example.benchmark

import com.mellonita.optimk.valueIn
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

class Rastrigin(d: Int) : Benchmark(d) {
    private val A = 5

    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.map { it.valueIn((-5.12).rangeTo(5.12)) }.toDoubleArray()
    }

    override fun objective(solution: DoubleArray): Double {
        return A * d + solution.sumOf { x -> x.pow(2) - A * cos(2 * PI * x) }
    }
}