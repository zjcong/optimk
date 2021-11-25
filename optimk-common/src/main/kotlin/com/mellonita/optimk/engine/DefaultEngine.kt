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
import com.mellonita.optimk.optimizer.OpenBorder
import com.mellonita.optimk.optimizer.Optimizer
import com.mellonita.optimk.problem.Problem


/**
 * Basic optimization engine
 * @param problem Problem to solve
 * @param goal Goal type, GOAL_MIN or GOAL_MAX
 */
public open class DefaultEngine<T>(
    override val problem: Problem<T>,
    override val goal: Goal,
    override val monitor: Monitor<T>,
    protected var optimizer: Optimizer
) : Engine<T>() {


    protected var population: Array<DoubleArray> = optimizer.initialize()
    protected var fitness: DoubleArray = doubleArrayOf()
    override var isOpen: Boolean = optimizer is OpenBorder

    /**
     * Single iteration
     */
    public override fun updateFitness() {
        fitness = population.map { evaluateIndividual(it) }.toDoubleArray()
        val min = fitness.withIndex().minByOrNull { it.value }!!
        if (min.value < bestFitness) {
            bestFitness = min.value
            bestSolution = population[min.index]
        }
    }

    /**
     * On immigrant arrival
     */
    override fun arrival(s: DoubleArray, f: Double): Boolean {
        if (!isOpen) {
            debug("Immigrant arrived but island is closed.")
            return false
        }
        //val targetIndex = (population.indices).random()
        val targetIndex = this.fitness.withIndex().minByOrNull { it.value }!!.index


        if (this.fitness[targetIndex] < f) {
            debug("Immigrant [$f] arrived but is worse than worst individual [${this.fitness[targetIndex]}].")
            return false
        }


        population[targetIndex] = s
        this.fitness[targetIndex] = f
        debug("Immigrant [$f] is accepted")
        return true
    }

    /**
     * Generate samples for next iteration
     */
    override fun nextIteration() {
        iterations++
        population = optimizer.iterate(population, fitness)
        debug("Iteration [$iterations] finished, fitness: [$bestFitness]")

    }

    /**
     * Start optimization
     */
    override fun optimize(): T {
        this.startTime = System.currentTimeMillis()
        debug("Engine start at timestamp [$startTime]")
        do {
            // Evaluate population
            updateFitness()
            // Sample next population
            nextIteration()
        } while (!monitor.stop(this))
        return problem.decode(bestSolution)
    }
}


