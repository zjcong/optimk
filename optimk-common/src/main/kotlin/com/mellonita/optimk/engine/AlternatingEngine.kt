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

import com.mellonita.optimk.monitor.Monitor
import com.mellonita.optimk.optimizer.Optimizer
import com.mellonita.optimk.problem.Problem
import kotlin.math.min


/**
 *
 */
public open class AlternatingEngine<T>(
    problem: Problem<T>,
    goal: Goal,
    private val optimizers: List<Optimizer>,
    private val threshold: Long,
    monitor: Monitor<T>,
) : DefaultEngine<T>(problem, goal, monitor, optimizers[0]) {

    private var stagnation: Int = 0
    private var activeOptimizerIndex: Int = 0

    init {
        require(optimizers.isNotEmpty())
        require(threshold > 0)
        require(optimizers.all { it.d == optimizers[0].d })
    }


    /**
     *
     */
    override fun updateFitness() {
        val lastBF = bestFitness
        super.updateFitness()
        if (bestFitness < lastBF) stagnation = 0
        else stagnation++
    }

    /**
     *
     */
    override fun nextIteration() {
        // Change optimizer
        if (stagnation > threshold) {
            activeOptimizerIndex++
            optimizer = optimizers[activeOptimizerIndex.rem(optimizers.size)]
            debug("Engine alternated to [${optimizer.javaClass.simpleName}]")
            val pIndices = fitness.withIndex().sortedBy { it.value }.map { it.index }
            val np = Array(min(optimizer.p, population.size)) { population[pIndices[it]] }
            population = optimizer.initialize(np)
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