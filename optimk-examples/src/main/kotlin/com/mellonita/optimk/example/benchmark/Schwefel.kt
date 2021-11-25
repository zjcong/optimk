package com.mellonita.optimk.example.benchmark

import com.mellonita.optimk.problem.valueIn
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

class Schwefel(d: Int) : Benchmark(d) {

    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.map { it.valueIn((-512.0).rangeTo(512.0)) }.toDoubleArray()
    }

    override fun objective(solution: DoubleArray): Double {
        return (418.982887 * d) - solution.sumOf { it * sin(sqrt(abs(it))) }
    }
}
