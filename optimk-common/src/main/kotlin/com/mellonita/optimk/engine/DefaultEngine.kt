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

import com.mellonita.optimk.Monitor
import com.mellonita.optimk.Problem
import com.mellonita.optimk.optimizer.Optimizer


/**
 * Basic optimization engine
 * @param problem Problem to solve
 * @param goal Goal type, GOAL_MIN or GOAL_MAX
 * @param optimizer Optimizer
 */
public open class DefaultEngine<T>(
    override val problem: Problem<T>,
    override val goal: Goal,
    private val optimizer: Optimizer,
    override val monitor: Monitor<T>,
) : Engine<T>() {

    private var currentGeneration = optimizer.initialize()

    /**
     *
     */
    private fun iterate(population: Array<DoubleArray>): Array<DoubleArray> {
        iterations++
        val fitness = population.map { evaluateIndividual(it) }.toDoubleArray()
        val min = fitness.withIndex().minByOrNull { it.value }!!
        if (min.value < bestFitness) {
            bestFitness = min.value
            bestSolution = population[min.index]
        }
        monitor.debug(population, fitness)
        return optimizer.iterate(population, fitness)
    }


    /**
     *
     */
    override fun optimize(): T {
        this.startTime = System.currentTimeMillis()
        do {
            currentGeneration = iterate(currentGeneration)
        } while (!monitor.stop(this))

        return problem.decode(bestSolution)
    }
}


