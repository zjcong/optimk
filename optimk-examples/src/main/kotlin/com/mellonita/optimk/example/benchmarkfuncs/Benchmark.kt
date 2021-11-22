package com.mellonita.optimk.example.benchmarkfuncs

import com.mellonita.optimk.Problem

abstract class Benchmark(val d: Int) : Problem<DoubleArray> {
    init {
        require(d > 0)
    }
}