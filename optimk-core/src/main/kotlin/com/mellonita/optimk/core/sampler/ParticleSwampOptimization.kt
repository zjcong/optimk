/*
 * Copyright (C) Zijie Cong 2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mellonita.optimk.core.sampler

import com.mellonita.optimk.core.OpenBorder
import com.mellonita.optimk.core.Sampler
import com.mellonita.optimk.core.math.minus
import com.mellonita.optimk.core.math.plus
import com.mellonita.optimk.core.math.times
import kotlin.random.Random


/**
 * Particle swamp optimization
 */
public class ParticleSwampOptimization(
    d: Int,
    p: Int,
    private val w: Double = 0.5,
    private val c1: Double = 2.0,
    private val c2: Double = 2.0,
    rng: Random = Random(0)
) : Sampler(d, p, rng), OpenBorder {

    private val pBest = Array(p) { Pair(doubleArrayOf(), Double.MAX_VALUE) }
    private var gBest = Pair(doubleArrayOf(), Double.MAX_VALUE)
    private val velocities = Array(p) { DoubleArray(d) { rng.nextDouble() } }


    public constructor(d: Int, p: Int, rng: Random) : this(d, p, 0.5, 2.0, 2.0, rng)

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
            if (pBest[i].second >= fitness[i]) pBest[i] = Pair(population[i], fitness[i])
            if (gBest.second >= fitness[i]) gBest = Pair(population[i], fitness[i])
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
        velocities.indices.forEach { i -> velocities[i] = DoubleArray(d) { rng.nextDouble() } }
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
        velocities.indices.forEach { i -> velocities[i] = DoubleArray(d) { rng.nextDouble() } }
        return randomPopulation
    }
}
