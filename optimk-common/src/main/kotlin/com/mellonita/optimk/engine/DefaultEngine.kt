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

import com.mellonita.optimk.*
import com.mellonita.optimk.problem.Problem
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random


/**
 * Basic optimization engine
 * @param problem Problem to solve
 * @param goal Goal type, GOAL_MIN or GOAL_MAX
 */
public open class DefaultEngine<T>(
    override val problem: Problem<T>,
    override val goal: Goal,
    override val monitor: Monitor<T>,
    protected var optimizer: Optimizer,
    protected val rng: Random = Random(0)
) : Engine<T>() {

    /**
     * Individuals
     */
    protected var population: Array<DoubleArray> = optimizer.initialize()

    /**
     * Fitness values of current population
     */
    protected var fitness: DoubleArray = doubleArrayOf()

    /**
     * This engine is open if the optimizer is open border
     */
    override var isOpen: Boolean = optimizer is OpenBorder

    /**
     * Update fitness
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
     * Upon immigrant arrival
     */
    override fun arrival(s: DoubleArray, f: Double): Boolean {
        if (!isOpen) {
            log(LogLevel.DEBUG, "Immigrant arrived but island is closed.")
            return false
        }
        val targetIndex = rng.nextInt(population.size)
        //val targetIndex = this.fitness.withIndex().maxByOrNull { it.value }!!.index //pick the worst individual
        if (this.fitness[targetIndex] < f) {
            log(
                LogLevel.DEBUG,
                "Immigrant [$f] arrived but is worse than worst individual [${this.fitness[targetIndex]}]."
            )
            return false
        }

        population[targetIndex] = s
        this.fitness[targetIndex] = f
        log(LogLevel.DEBUG, "Immigrant [$f] is accepted")
        return true
    }

    /**
     * Generate samples for next iteration
     */
    override fun nextIteration() {
        iterations++
        population = optimizer.iterate(population, fitness)
        log(LogLevel.DEBUG, "Iteration [$iterations] finished, fitness: [$bestFitness]")

    }

    /**
     * Start optimization
     */
    override fun optimize(): T {
        this.startTime = System.currentTimeMillis()
        log(
            LogLevel.INFO,
            "Engine start at timestamp [${
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(startTime),
                    ZoneId.systemDefault()
                )
            }]"
        )
        do {
            // Evaluate population
            updateFitness()
            // Sample next population
            nextIteration()
        } while (!monitor.stop(this))
        log(LogLevel.INFO, "Engine terminated with best fitness [$bestFitness]")
        return problem.decode(bestSolution)
    }
}


