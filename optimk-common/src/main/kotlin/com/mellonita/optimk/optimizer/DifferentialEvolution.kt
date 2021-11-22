@file:Suppress("unused")

package com.mellonita.optimk.optimizer

import com.mellonita.optimk.*
import kotlin.random.Random

typealias MutationStrategy = (Array<DoubleArray>, DoubleArray, Random) -> Array<DoubleArray>

/**
 *
 */
class DifferentialEvolution @JvmOverloads constructor(
    private val dimensions: Int,
    private val population: Int,
    private val mutation: MutationStrategy,
    private val rng: Random = Random(System.currentTimeMillis()),
    private val cr: Double = 0.8,
) : Optimizer, OpenBorder {


    /**
     *
     */
    override fun iterate(currentGeneration: Array<DoubleArray>, fitnessValues: DoubleArray): Array<DoubleArray> {

        require(currentGeneration.size == population * 2) { "Invalid population" }
        require(fitnessValues.size == currentGeneration.size)

        val p = currentGeneration.sliceArray(0 until population)
        val fp = fitnessValues.sliceArray(0 until population)
        val t = currentGeneration.sliceArray(population until currentGeneration.size)
        val ft = fitnessValues.sliceArray(population until currentGeneration.size)

        val n = Array(population) { doubleArrayOf() }
        val fn = DoubleArray(population)

        //Selection
        p.indices.forEach { i ->
            n[i] = if (fp[i] < ft[i]) p[i] else t[i]
            fn[i] = if (fp[i] < ft[i]) fp[i] else ft[i]
        }


        // Mutation and crossover
        val tn = mutation(n, fn, rng).withIndex().map { m ->
            check(m.value.size == dimensions)
            val i = m.index
            val mutation = m.value
            val jRand = rng.nextInt(0, dimensions)
            DoubleArray(dimensions) { j ->
                if (rng.nextDouble() >= cr && j != jRand) n[i][j]
                else mutation[j]
            }
        }.toTypedArray()

        return n.plus(tn)
    }

    /**
     *
     */
    override fun initialize(): Array<DoubleArray> {
        return Array(population * 2) { DoubleArray(dimensions) { rng.nextDouble() } }
    }


    /**
     * Mutation Strategies
     */
    companion object {
        /**
         * DE/rand/1
         */
        fun rand1(f: Double): MutationStrategy =
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
        fun best1(f: Double): MutationStrategy =
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
        fun best2(f1: Double, f2: Double) =
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
        fun currentToRand1(f1: Double, f2: Double) =
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
        fun currentToBest1(f1: Double, f2: Double) =
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