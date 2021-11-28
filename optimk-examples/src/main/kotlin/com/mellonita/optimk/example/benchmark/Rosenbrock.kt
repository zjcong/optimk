package com.mellonita.optimk.example.benchmark

import kotlin.math.pow

class Rosenbrock(d: Int) : Benchmark(d) {

    override val lowerBounds: DoubleArray = DoubleArray(d) { -5.0 }
    override val globalOptima: Double = 0.0
    override val upperBounds: DoubleArray = DoubleArray(d) { 10.0 }

    override fun objective(solution: DoubleArray): Double {
        val X = solution
        var sum = 0.0
        for (i in 1 until solution.size - 1) {
            sum += 100.0 * (X[i + 1] - X[i].pow(2)).pow(2) + (1 - X[i]).pow(2)
        }
        return sum
    }
}