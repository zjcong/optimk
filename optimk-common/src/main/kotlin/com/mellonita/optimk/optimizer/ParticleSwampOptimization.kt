package com.mellonita.optimk.optimizer

import com.mellonita.optimk.minus
import com.mellonita.optimk.plus
import com.mellonita.optimk.times
import kotlin.random.Random


/**
 * Particle swamp optimization
 */
public class ParticleSwampOptimization(
    d: Int,
    p: Int,
    private val w: Double = 0.8,
    private val c1: Double = 0.1,
    private val c2: Double = 0.1,
    rng: Random = Random(System.currentTimeMillis())
) : Optimizer(d, p, rng), OpenBorder {

    private val pBest = Array(p) { Pair(doubleArrayOf(), Double.MAX_VALUE) }
    private var gBest = Pair(doubleArrayOf(), Double.MAX_VALUE)
    private val velocities = Array(p) { DoubleArray(p) { rng.nextDouble() } }

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
    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {
        //update pBest and gBest
        fitness.indices.forEach { i ->
            if (pBest[i].second > fitness[i]) pBest[i] = Pair(population[i], fitness[i])
            if (gBest.second > fitness[i]) gBest = Pair(population[i], fitness[i])
        }
        //update velocities
        updateVelocities(population)
        return population.indices.map { i -> population[i] + velocities[i] }.toTypedArray()
    }

}