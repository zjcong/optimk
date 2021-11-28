package com.mellonita.optimk.example.benchmark

import com.mellonita.optimk.problem.Goal
import com.mellonita.optimk.problem.Problem
import com.mellonita.optimk.valueIn


sealed class Benchmark(final override val dimensions: Int) : Problem<DoubleArray> {

    override val goal: Goal = Goal.Minimize

    abstract val upperBounds: DoubleArray
    abstract val lowerBounds: DoubleArray

    abstract val globalOptima: Double

    init {
        require(dimensions > 0)
    }

    override fun decode(keys: DoubleArray): DoubleArray {
        return DoubleArray(dimensions) { i ->
            keys[i].valueIn(lowerBounds[i].rangeTo(upperBounds[i]))
        }
    }
}