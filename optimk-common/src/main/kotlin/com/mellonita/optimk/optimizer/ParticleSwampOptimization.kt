package com.mellonita.optimk.optimizer

import com.mellonita.optimk.*
import kotlin.random.Random


/**
 * Classic particle swamp optimization
 */
class ParticleSwampOptimization(
    val dimensions: Int,
    val population: Int,
    val w: Double = 0.8,
    val c1: Double = 0.1,
    val c2: Double = 0.1,
    val rng: Random = Random(System.currentTimeMillis())
) : Optimizer, OpenBorder {

    private val pBest = Array(population) { Pair(doubleArrayOf(), Double.MAX_VALUE) }
    private var gBest = Pair(doubleArrayOf(), Double.MAX_VALUE)
    private val velocities = Array(population) { DoubleArray(dimensions) { rng.nextDouble() } }

    /**
     *
     */
    private fun updateVelocities(currentGeneration: Array<DoubleArray>) {
        currentGeneration.indices.forEach { i ->
            val pb = pBest[i].first
            val v = velocities[i]
            val r1 = rng.nextDouble()
            val r2 = rng.nextDouble()
            val x = currentGeneration[i]
            val vn = w * v + c1 * r1 * (pb - x) + c2 * r2 * (gBest.first - x)
            velocities[i] = vn
        }

    }

    /**
     *
     */
    override fun iterate(currentGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray> {

        //update pBest and gBest
        fitnessValues.indices.forEach { i ->
            if (pBest[i].second > fitnessValues[i]) pBest[i] = Pair(currentGeneration[i], fitnessValues[i])
            if (gBest.second > fitnessValues[i]) gBest = Pair(currentGeneration[i], fitnessValues[i])
        }

        updateVelocities(currentGeneration)

        val n = currentGeneration.indices.map { i -> currentGeneration[i] + velocities[i] }.toTypedArray()
        check(n.size == population)
        return n
    }


    /**
     *
     */
    override fun initialize(): Array<DoubleArray> {
        return Array(population) { DoubleArray(dimensions) { rng.nextDouble() } }
    }
}