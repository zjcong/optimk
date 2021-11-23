package com.mellonita.optimk.example.benchmark

import com.mellonita.optimk.valueIn
import kotlin.math.pow

class Rosenbrock(d: Int) : Benchmark(d) {
    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.map { it.valueIn((-5.0).rangeTo(10.0)) }.toDoubleArray()
    }

    override fun objective(solution: DoubleArray): Double {
        val X = solution
        return (1 until solution.size - 1).sumOf { i ->
            100.0 * (X[i + 1] - X[i].pow(2)).pow(2) + (1 - X[i]).pow(2)
        }
    }

}