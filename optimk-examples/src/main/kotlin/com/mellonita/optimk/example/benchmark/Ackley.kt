package com.mellonita.optimk.example.benchmark

import kotlin.math.*

class Ackley(d: Int) : Benchmark(d) {

    override val lowerBounds: DoubleArray = DoubleArray(d) { -32.70 }
    override val globalOptima: Double = 0.0
    override val upperBounds: DoubleArray = DoubleArray(d) { 32.70 }

    private var a = 20.0
    private var b = 0.2
    private var c = 2.0 * PI

    override fun objective(solution: DoubleArray): Double {
        var sum1 = 0.0
        var sum2 = 0.0
        for (i in 0 until dimensions) {
            sum1 += solution[i].pow(2.0)
            sum2 += cos(c * solution[i])
        }
        return -20.0 * exp(-0.2 * sqrt(sum1 / dimensions.toDouble())) + 20 - exp(sum2 / dimensions.toDouble()) + exp(1.0)

    }


}

