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
 * Island Model Engine
 */
public open class IslandEngine<T>(
    override val problem: Problem<T>,
    override val goal: Goal,
    protected val islands: List<Engine<T>>,
    protected val migrationInterval: Int,
    override val monitor: Monitor<T>,
    protected val rng: Random = Random(0),
) : Engine<T>() {

    /**
     * Collection of open islands
     */
    private val openIslands: List<Engine<T>> = islands.filter { it.isOpen }

    /**
     * If this engine is open
     */
    override val isOpen: Boolean = openIslands.isNotEmpty()

    /**
     * Perform optimization
     */
    override fun optimize(): T {
        startTime = System.currentTimeMillis()
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
            updateFitness()
            nextIteration()
        } while (!monitor.stop(this))
        return problem.decode(bestSolution)
    }

    /**
     * Migrate the best individual from a randomly chosen island (origin) to another (destination)
     * Destination island must be marked with OpenBorder interface. If no island is open then this function returns
     * immediately without migration
     */
    public open fun migrate() {
        if (openIslands.isEmpty()) return
        val destination = openIslands[rng.nextInt(openIslands.size)]
        val origin = islands[rng.nextInt(islands.size)]
        if (destination != origin)
            destination.arrival(origin.bestSolution, bestFitness)
    }

    /**
     * Perform evaluation of individuals on each island and update overall best individual.
     */
    override fun updateFitness() {
        // Update number of evaluations
        evaluations = islands.sumOf { it.evaluations }
        // Evaluate islands
        islands.forEach { it.updateFitness() }
        // Update best individual
        val min = islands.minByOrNull { it.bestFitness }!!
        if (min.bestFitness < bestFitness) {
            bestFitness = min.bestFitness
            bestSolution = min.bestSolution
        }
    }

    /**
     * Next iteration of sampling
     */
    override fun nextIteration() {
        iterations++
        // migrate
        if (iterations != 0L && iterations.rem(migrationInterval) == 0L)
            migrate()
        islands.forEach { it.nextIteration() }
        log(LogLevel.DEBUG, "Iteration [$iterations] finished, fitness: [$bestFitness]")
    }

    /**
     * Upon arrival of a migrant, it will be sent to an open island
     * @param s Solution
     * @param f Fitness value of s
     * @return if the migrant is accepted
     */
    override fun arrival(s: DoubleArray, f: Double): Boolean {
        if (!isOpen) return false
        val destination = openIslands[rng.nextInt(openIslands.size)]
        return destination.arrival(s, f)
    }
}

/**
 *
 */
public fun <T> islandsOf(
    n: Int,
    problem: Problem<T>,
    goal: Goal,
    monitor: Monitor<T>,
    optimizers: List<Optimizer>
): List<Engine<T>> {
    return (0 until n).map {
        DefaultEngine(
            goal = goal,
            problem = problem,
            optimizer = optimizers[it.rem(optimizers.size)],
            monitor = monitor
        )
    }
}