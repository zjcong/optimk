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
import com.mellonita.optimk.core.Stateless
import com.mellonita.optimk.core.Problem
import com.mellonita.optimk.core.Sampler
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random


/**
 * Basic optimization engine
 * @param problem Problem to solve
 * @param monitor Monitor
 * @param sampler
 * @param rng Random Number Generator
 */
public open class DefaultEngine<T>(
    override val name: String,
    override val problem: Problem<T>,
    override val monitor: Monitor<T>,
    protected var sampler: Sampler,
    protected val rng: Random = Random(0)
) : com.mellonita.optimk.core.Engine<T>() {

    /**
     * Individuals
     */
    protected var population: Array<DoubleArray> = sampler.initialize()

    /**
     * Fitness values of current population
     */
    protected var fitness: DoubleArray = doubleArrayOf()

    /**
     * Update fitness
     */
    public override fun updateFitness() {
        fitness = evaluatePopulation(population)
        val min = fitness.withIndex().minByOrNull { it.value }!!
        if (min.value < bestFitness || bestFitness == Double.MAX_VALUE) {
            bestFitness = min.value
            bestSolution = population[min.index]
        }
        monitor.onIteration(this)
    }

    /**
     * Upon immigrant arrival
     */
    override fun arrival(s: DoubleArray, f: Double): Boolean {
        val targetIndex = rng.nextInt(population.size)
        if (fitness[targetIndex] < f) {
            debug("Immigrant [$f] is worse than target individual [${this.fitness[targetIndex]}].")
        }

        if (sampler !is Stateless) {
            debug("Immigrant arrived but island is closed.")
            return false
        }
        population[targetIndex] = s
        fitness[targetIndex] = f
        debug("Immigrant [$f] is admitted")
        return true
    }

    /**
     * Generate samples for next iteration
     */
    override fun nextIteration() {
        iterations++
        population = sampler.iterate(population, fitness)
        debug("Iteration [$iterations] finished, fitness: [$bestFitness]")
    }

    /**
     * Start optimization
     */
    override fun optimize(): T {
        this.startTime = System.currentTimeMillis()
        info(
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
        info("Engine terminated with best fitness [$bestFitness] after [${System.currentTimeMillis() - startTime}]ms")
        return problem.decode(bestSolution)
    }
}


