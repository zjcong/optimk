package com.mellonita.optimk.optimizer

import com.mellonita.optimk.*
import kotlin.random.Random


/**
 * Particle swamp optimization
 */
public class ParticleSwampOptimization(
    dimensionality: Int,
    population: Int,
    private val w: Double = 0.5,
    private val c1: Double = 2.0,
    private val c2: Double = 2.0,
    rng: Random = Random(0)
) : Optimizer(dimensionality, population, rng), OpenBorder {

    private val pBest = Array(population) { Pair(doubleArrayOf(), Double.MAX_VALUE) }
    private var gBest = Pair(doubleArrayOf(), Double.MAX_VALUE)
    private val velocities = Array(population) { DoubleArray(dimensionality) { rng.nextDouble() } }

    /**
     *
     */
    private fun updateVelocities(population: Array<DoubleArray>) {
        population.indices.forEach { i ->
            val pb = pBest[i].first
            val v = velocities[i]
            val r1 = rng.nextDouble()
            val r2 = rng.nextDouble()
            val x = population[i]
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

    /**
     *
     */
    override fun initialize(): Array<DoubleArray> {
        val randomPopulation = super.initialize()
        // Clear history
        pBest.indices.forEach { i -> pBest[i] = Pair(doubleArrayOf(), Double.MAX_VALUE) }
        gBest = Pair(doubleArrayOf(), Double.MAX_VALUE)
        velocities.indices.forEach { i -> velocities[i] = DoubleArray(dimensionality) { rng.nextDouble() } }
        return randomPopulation
    }

    /**
     *
     */
    override fun initialize(init: Array<DoubleArray>): Array<DoubleArray> {
        val randomPopulation = super.initialize(init)
        // Clear history
        pBest.indices.forEach { i -> pBest[i] = Pair(doubleArrayOf(), Double.MAX_VALUE) }
        gBest = Pair(doubleArrayOf(), Double.MAX_VALUE)
        velocities.indices.forEach { i -> velocities[i] = DoubleArray(dimensionality) { rng.nextDouble() } }
        return randomPopulation
    }
}
