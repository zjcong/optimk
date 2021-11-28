package com.mellonita.optimk.example.benchmark

import kotlin.math.pow

class Sphere(d: Int) : Benchmark(d) {


    override val lowerBounds: DoubleArray = DoubleArray(d) { -5.12 }
    override val globalOptima: Double = 0.0
    override val upperBounds: DoubleArray = DoubleArray(d) { 5.12 }


    override fun objective(solution: DoubleArray): Double {
        var sum = 0.0
        for (i in solution.indices) {
            sum += solution[i].pow(2)
        }
        return sum
    }

}