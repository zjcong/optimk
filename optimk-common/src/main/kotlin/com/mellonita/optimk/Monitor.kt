package com.mellonita.optimk

interface Monitor {

    fun stop(info: IterationInfo): Boolean

    fun report(info: IterationInfo, population: Array<DoubleArray>, fitnessValues: DoubleArray)
}