package com.mellonita.optimk.example.benchmark

import com.mellonita.optimk.problem.valueIn
import kotlin.math.pow

class Sphere(d: Int) : Benchmark(d) {

    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.map { it.valueIn((-5.12).rangeTo(5.12)) }.toDoubleArray()
    }

    override fun objective(solution: DoubleArray): Double = solution.sumOf { it.pow(2) }

}