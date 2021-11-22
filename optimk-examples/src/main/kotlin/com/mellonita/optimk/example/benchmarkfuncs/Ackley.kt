package com.mellonita.optimk.example.benchmarkfuncs

import com.mellonita.optimk.valueIn
import kotlin.math.*

class Ackley(d: Int) : Benchmark(d) {

    private var a = 20
    private var b = 0.2
    private var c = 2 * PI

    override fun decode(keys: DoubleArray): DoubleArray =
        keys.map { x -> x.valueIn((-32.7).rangeTo(32.7)) }.toDoubleArray()

    override fun objective(candidate: DoubleArray): Double {
        var sum1 = 0.0
        var sum2 = 0.0
        for (i in 0 until d) {
            sum1 += candidate[i].pow(2.0)
            sum2 += cos(c * candidate[i])
        }
        return a + exp(1.0) - a * exp(-b * sqrt(sum1 / d)) - exp(sum2 / d)
    }
}