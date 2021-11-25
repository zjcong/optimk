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


@file:Suppress("MemberVisibilityCanBePrivate")

package com.mellonita.optimk.engine

import com.mellonita.optimk.monitor.Monitor
import com.mellonita.optimk.problem.Problem
import kotlin.random.Random


/**
 * Island Model Engine
 */
public open class IslandEngine<T>(
    override val problem: Problem<T>,
    override val goal: Goal,
    protected val islands: List<Engine<T>>,
    protected val migrationInterval: Int,
    protected val rng: Random = Random(System.currentTimeMillis()),
    override val monitor: Monitor<T>,
) : Engine<T>() {

    //Open Island
    protected val openIslands: List<Engine<T>> = islands.filter { it.isOpen }

    override val isOpen: Boolean = openIslands.isNotEmpty()

    /**
     * Perform optimization
     */
    override fun optimize(): T {
        startTime = System.currentTimeMillis()
        debug("Engine start at [$startTime]")
        do {
            updateFitness()
            nextIteration()
        } while (!monitor.stop(this))
        return problem.decode(bestSolution)
    }

    /**
     *
     */
    public open fun migrate() {
        if (openIslands.isEmpty()) return
        val n = rng.nextInt(openIslands.size)
        repeat(n) {
            val destination = openIslands[rng.nextInt(openIslands.size)]
            val origin = islands[rng.nextInt(islands.size)]
            if (destination != origin)
                destination.arrival(origin.bestSolution, bestFitness)
        }
    }


    override fun updateFitness() {
        val min = islands.minByOrNull { it.bestFitness }!!
        if (min.bestFitness < bestFitness) {
            bestFitness = min.bestFitness
            bestSolution = min.bestSolution
        }
        // Evaluate islands
        evaluations = islands.sumOf { it.evaluations }
        islands.parallelStream().forEach { it.updateFitness() }
    }

    override fun nextIteration() {
        iterations++
        // migrate
        if (iterations != 0L && iterations.rem(migrationInterval) == 0L)
            migrate()
        islands.parallelStream().forEach { it.nextIteration() }
        debug("Iteration [$iterations] finished, fitness: [$bestFitness]")
    }

    /**
     *
     */
    override fun arrival(s: DoubleArray, f: Double): Boolean {
        if (!isOpen) return false
        val destination = openIslands[rng.nextInt(openIslands.size)]
        return destination.arrival(s, f)
    }
}