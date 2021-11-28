package com.mellonita.optimk.example.benchmark

import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt


class Schwefel(d: Int) : Benchmark(d) {

    override val lowerBound: Double = -500.0
    override val upperBound: Double = 500.0

    override val globalOptima: Double = d * 0.4971429158184719

    override fun objective(solution: DoubleArray): Double {
        var sum = 0.0
        for (i in 0 until dimensions) {
            sum += -solution[i] * sin(sqrt(abs(solution[i])))
        }
        return sum + dimensions * 4.18982887272434686131e+02
    }
}