package com.mellonita.optimk.example.benchmark

import com.mellonita.optimk.Problem

abstract class Benchmark(override val d: Int) : Problem<DoubleArray> {

    init {
        require(d > 0)
    }

}