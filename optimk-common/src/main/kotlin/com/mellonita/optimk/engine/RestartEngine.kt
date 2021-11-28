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
import kotlin.random.Random

/**
 * Restart Engine
 */
public open class RestartEngine<T>(
    problem: Problem<T>,
    optimizer: Optimizer,
    monitor: Monitor<T>,
    private val threshold: Int,
    rng: Random = Random(0)
) : DefaultEngine<T>(problem, monitor, optimizer, rng) {

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
     *
     */
    override fun nextIteration() {
        if (stagnation > /*threshold*/ iterations / threshold + threshold) {
            log(LogLevel.INFO, "Engine restart at iteration $iterations")
            population = optimizer.initialize(arrayOf(bestSolution))
            updateFitness()
            stagnation = 0
        }
        super.nextIteration()
    }
}