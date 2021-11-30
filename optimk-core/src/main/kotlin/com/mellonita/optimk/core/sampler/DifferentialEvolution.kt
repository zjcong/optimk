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

@file:Suppress("unused")

package com.mellonita.optimk.core.sampler

import com.mellonita.optimk.core.OpenBorder
import com.mellonita.optimk.core.Optimizer
import com.mellonita.optimk.core.math.minus
import com.mellonita.optimk.core.math.plus
import com.mellonita.optimk.core.math.times
import kotlin.random.Random

public typealias MutationStrategy = (Array<DoubleArray>, DoubleArray, Random) -> Array<DoubleArray>

/**
 *
 */
public class DifferentialEvolution @JvmOverloads constructor(
    d: Int,
    p: Int,
    private val cr: Double = 0.8,
    private val mutation: MutationStrategy,
    rng: Random = Random(0)
) : Optimizer(d, p, rng), OpenBorder {

    public constructor(d: Int, p: Int, rng: Random) : this(d, p, 0.8, mutation = DifferentialEvolution.best1(0.7), rng)

    /**
     *
     */
    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {

        require(population.size == this.p * 2) { "Invalid population" }
        require(fitness.size == population.size)

        val pp = population.sliceArray(0 until this.p)
        val fp = fitness.sliceArray(0 until this.p)
        val t = population.sliceArray(this.p until population.size)
        val ft = fitness.sliceArray(this.p until population.size)

        val n = Array(this.p) { doubleArrayOf() }
        val fn = DoubleArray(this.p)

        //Selection
        pp.indices.forEach { i ->
            n[i] = if (fp[i] < ft[i]) pp[i] else t[i]
            fn[i] = if (fp[i] < ft[i]) fp[i] else ft[i]
        }


        // Mutation and crossover
        val tn = mutation(n, fn, rng).withIndex().map { m ->
            check(m.value.size == d)
            val i = m.index
            val mutation = m.value
            val jRand = rng.nextInt(0, d)
            DoubleArray(d) { j ->
                if (rng.nextDouble() < cr || j == jRand) mutation[j]
                else n[i][j]
            }
        }.toTypedArray()

        return n.plus(tn)
    }

    /**
     *
     */
    override fun initialize(): Array<DoubleArray> {
        return Array(p * 2) { DoubleArray(d) { rng.nextDouble() } }
    }

    /**
     *
     */
    override fun initialize(init: Array<DoubleArray>): Array<DoubleArray> {
        if (init.size >= p * 2) return init.sliceArray(0 until p * 2)
        return Array(p * 2) {
            if (it < init.size) init[it]
            else DoubleArray(d) { rng.nextDouble() }
        }
    }

    /**
     * Mutation Strategies
     */
    public companion object {
        /**
         * DE/rand/1
         */
        public fun rand1(f: Double): MutationStrategy =
            fun(g: Array<DoubleArray>, _: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 3)
                return Array(g.size) {
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 3) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    parents[0] + f * (parents[1] - parents[2])
                }
            }

        /**
         * DE/best/1
         */
        public fun best1(f: Double): MutationStrategy =
            fun(g: Array<DoubleArray>, fit: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 3)
                val best = g[fit.withIndex().minByOrNull { it.value }!!.index]
                return Array(g.size) {
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 2) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    best + f * (parents[0] - parents[1])
                }
            }


        /**
         * DE/best/2
         */
        public fun best2(f1: Double, f2: Double): MutationStrategy =
            fun(g: Array<DoubleArray>, fit: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 5)
                val best = g[fit.withIndex().minByOrNull { it.value }!!.index]
                return Array(g.size) {
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 4) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    best + f1 * (parents[0] - parents[1]) + f2 * (parents[2] - parents[3])
                }
            }


        /**
         * DE/current-to-rand/1
         */
        public fun currentToRand1(f1: Double, f2: Double): MutationStrategy =
            fun(g: Array<DoubleArray>, _: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 5)
                return Array(g.size) { i ->
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 4) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    g[i] + f1 * (parents[0] - parents[1]) + f2 * (parents[2] - parents[3])
                }
            }

        /**
         * DE/current-to-best/1
         */
        public fun currentToBest1(f1: Double, f2: Double): MutationStrategy =
            fun(g: Array<DoubleArray>, fit: DoubleArray, rng: Random): Array<DoubleArray> {
                require(g.size > 5)
                val best = g[fit.withIndex().minByOrNull { it.value }!!.index]
                return Array(g.size) { i ->
                    val parentIndices = mutableSetOf<Int>()
                    while (parentIndices.size < 4) parentIndices.add(rng.nextInt(0, g.size))
                    val parents = parentIndices.map { g[it] }
                    g[i] + f1 * (best - parents[1]) + f2 * (parents[2] - parents[3])
                }
            }
    }
}