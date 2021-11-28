package com.mellonita.optimk.example.benchmark

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

class Rastrigin(d: Int) : Benchmark(d) {

    private val A = 10
    override val lowerBound: Double = -5.12
    override val upperBound: Double = 5.12
    override val globalOptima: Double = 0.0

    override fun objective(solution: DoubleArray): Double {

        var sum = 0.0
        for (i in solution.indices) {
            sum += solution[i].pow(2) - A * cos(2.0 * PI * solution[i])
        }
        return A * dimensions + sum
    }
}