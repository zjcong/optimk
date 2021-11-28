package com.mellonita.optimk.example.benchmark

import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

class Schwefel(d: Int) : Benchmark(d) {

    override val lowerBounds: DoubleArray = DoubleArray(d) { -500.0 }
    override val globalOptima: Double = 0.0
    override val upperBounds: DoubleArray = DoubleArray(d) { 500.0 }


    override fun objective(solution: DoubleArray): Double {
        var sum = 0.0
        for (i in solution.indices) {
            sum += -1.0 * solution[i] * sin(sqrt(abs(solution[i])))
        }
        return (418.982887 * dimensions) + sum
    }
}
