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
import kotlin.random.Random

/**
 * Restart Engine
 */
public open class RestartEngine<T>(
    override val name: String,
    problem: Problem<T>,
    sampler: Sampler,
    monitor: Monitor<T>,
    @Suppress("MemberVisibilityCanBePrivate") public val threshold: Int,
    rng: Random = Random(0)
) : DefaultEngine<T>(name, problem, monitor, sampler, rng) {

    private var stagnation: Int = 0
    private var totalStagnation: Int = 0


    /**
     *
     */
    override fun updateFitness() {
        val lastBF = bestFitness
        super.updateFitness()
        if (bestFitness < lastBF) stagnation = 0
        else {
            stagnation++
            totalStagnation += stagnation
        }
    }

    /**
     * Restart engine
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun restart() {
        if (stagnation > threshold) {
            info("Engine restart at iteration [$iterations]")
            population = sampler.initialize(arrayOf(bestSolution))
            updateFitness()
            stagnation = 0
        }
    }

    /**
     *
     */
    override fun nextIteration() {
        restart()
        super.nextIteration()
    }
}