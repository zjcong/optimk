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

package com.mellonita.optimk.island

import com.mellonita.optimk.Monitor
import com.mellonita.optimk.Problem
import com.mellonita.optimk.engine.Engine
import com.mellonita.optimk.engine.Goal
import com.mellonita.optimk.optimizer.OpenBorder
import com.mellonita.optimk.optimizer.Optimizer

/**
 *
 */
public abstract class Island<T>(
    override val problem: Problem<T>,
    override val goal: Goal,
    public val optimizer: Optimizer
) : Engine<T>() {

    public val isOpen: Boolean = optimizer is OpenBorder

    protected var population: Array<DoubleArray> = optimizer.initialize()
    protected var fitness: DoubleArray = DoubleArray(population.size) { evaluateIndividual(population[it]) }

    /**
     *
     */
    public abstract fun iterate()

    /**
     *
     */
    public fun evaluate() {
        fitness = population.map { evaluateIndividual(it) }.toDoubleArray()
        val min = fitness.withIndex().minByOrNull { it.value }!!
        if (min.value < bestFitness) {
            bestFitness = min.value
            bestSolution = population[min.index]
        }
    }

    /**
     *
     */
    public open fun arrival(migrant: DoubleArray, fitness: Double) {
        val worstIndex = this.fitness.withIndex().maxByOrNull { it.value }!!.index
        population[worstIndex] = migrant
        this.fitness[worstIndex] = fitness
    }

    override fun optimize(): T = problem.decode(bestSolution)


    override val monitor: Monitor<T> = object : Monitor<T> {
        override fun stop(engine: Engine<T>): Boolean = false
    }
}