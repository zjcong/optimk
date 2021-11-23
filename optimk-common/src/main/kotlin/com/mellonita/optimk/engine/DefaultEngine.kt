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
import com.mellonita.optimk.optimizer.OpenBorder
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
) : Island<T>() {

    private var population: Array<DoubleArray> = optimizer.initialize()
    private var fitness: DoubleArray = doubleArrayOf()
    override var isOpen: Boolean = optimizer is OpenBorder

    /**
     * Single iteration
     */
    public override fun evaluatePopulation() {
        iterations++
        fitness = population.map { evaluateIndividual(it) }.toDoubleArray()
        val min = fitness.withIndex().minByOrNull { it.value }!!
        if (min.value < bestFitness) {
            bestFitness = min.value
            bestSolution = population[min.index]
        }
        monitor.debug(population, fitness)
    }

    /**
     *
     */
    override fun arrival(s: DoubleArray, f: Double): Boolean {
        if (!isOpen) return false
        val worst = this.fitness.withIndex().minByOrNull { it.value }!!.index
        if (this.fitness[worst] >= f) return false
        population[worst] = s
        this.fitness[worst] = f
        return true
    }

    /**
     *
     */
    override fun nextIteration(current: Array<DoubleArray>): Array<DoubleArray> =
        optimizer.iterate(population, fitness)

    /**
     *
     */
    override fun optimize(): T {
        this.startTime = System.currentTimeMillis()
        do {
            // Evaluate population
            evaluatePopulation()

            // Sample next population
            population = nextIteration(population)
        } while (!monitor.stop(this))

        return problem.decode(bestSolution)
    }


}


