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

package com.mellonita.optimk.engine

import com.mellonita.optimk.LogLevel
import com.mellonita.optimk.Monitor
import com.mellonita.optimk.Optimizer
import com.mellonita.optimk.Problem
import kotlin.math.min
import kotlin.random.Random


/**
 * Alternating engine
 */
public open class AlternatingEngine<T>(
    problem: Problem<T>,
    private val optimizers: List<Optimizer>,
    private val threshold: Int,
    monitor: Monitor<T>,
    rng: Random = Random(0)
) : DefaultEngine<T>(problem, monitor, optimizers[0], rng) {

    /**
     * Number of iterations of unchanged best fitness
     */
    private var stagnation: Int = 0

    /**
     * Index of active optimizer in optimizers list
     */
    private var activeOptimizerIndex: Int = 0

    init {
        require(optimizers.isNotEmpty()) { "At least one optimizer must be specified" }
        require(threshold > 0) { "Stagnation threshold must be greater than zero" }
        require(optimizers.all { it.d == optimizers[0].d }) { "Optimizers must have consistent dimensionality" }
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
     * Next iteration of sampling
     */
    override fun nextIteration() {
        // Change optimizer
        if (stagnation > threshold) {
            activeOptimizerIndex++
            //optimizer = optimizers[activeOptimizerIndex.rem(optimizers.size)]
            optimizer = optimizers[rng.nextInt(optimizers.size)]
            val pIndices = fitness.withIndex().sortedBy { it.value }.map { it.index }
            val np = Array(min(optimizer.p, population.size)) { population[pIndices[it]] }
            population = optimizer.initialize(np)

            log(
                LogLevel.INFO,
                "Engine alternated to [${optimizer.javaClass.simpleName}] with population of [${np.size}]"
            )
            updateFitness()
            stagnation = 0
        }

        super.nextIteration()
    }

    /**
     *
     */
    override fun arrival(s: DoubleArray, f: Double): Boolean = false
}