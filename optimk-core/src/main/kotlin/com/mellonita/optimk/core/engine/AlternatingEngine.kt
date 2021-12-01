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

package com.mellonita.optimk.core.engine


import com.mellonita.optimk.core.Monitor
import com.mellonita.optimk.core.Problem
import com.mellonita.optimk.core.Sampler
import kotlin.math.min
import kotlin.random.Random


/**
 * Alternating engine
 */
public open class AlternatingEngine<T>(
    override val name: String,
    problem: Problem<T>,
    private val samplers: List<Sampler>,
    private val threshold: Int,
    monitor: Monitor<T>,
    rng: Random = Random(0)
) : DefaultEngine<T>(name, problem, monitor, samplers[0], rng) {

    /**
     * Number of iterations of unchanged best fitness
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected var stagnation: Int = 0

    /**
     * Index of active optimizer in optimizers list
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected var activeOptimizerIndex: Int = 0

    init {
        require(samplers.isNotEmpty()) { "At least one optimizer must be specified" }
        require(threshold > 0) { "Stagnation threshold must be greater than zero" }
        require(samplers.all { it.d == samplers[0].d }) { "Optimizers must have consistent dimensionality" }
    }


    /**
     * Update fitness values and stagnation count
     */
    override fun updateFitness() {
        val lastBF = bestFitness
        super.updateFitness()
        if (bestFitness < lastBF) stagnation = 0
        else stagnation++
    }


    /**
     * Alternation policy
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun alternate() {
        // Change sampler
        if (stagnation > threshold) {
            activeOptimizerIndex++
            sampler = samplers[activeOptimizerIndex.rem(samplers.size)]
            val pIndices = fitness.withIndex().sortedBy { it.value }.map { it.index }
            val np = Array(min(sampler.p, population.size)) { population[pIndices[it]] }
            population = sampler.initialize(np)
            info("Engine alternated to [${sampler.javaClass.simpleName}] with population of [${np.size}]]")
            updateFitness()
            stagnation = 0
        }
    }

    /**
     * Next iteration of sampling
     */
    override fun nextIteration() {
        alternate()
        super.nextIteration()
    }

    /**
     *
     */
    override fun arrival(s: DoubleArray, f: Double): Boolean = false
}