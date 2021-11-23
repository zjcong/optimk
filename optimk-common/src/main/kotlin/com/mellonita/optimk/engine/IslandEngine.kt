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

import com.mellonita.optimk.Monitor
import com.mellonita.optimk.Problem
import com.mellonita.optimk.island.SimpleIsland
import com.mellonita.optimk.optimizer.Optimizer
import kotlin.random.Random


/**
 * Island Model Engine
 */
public open class IslandEngine<T>(
    override val problem: Problem<T>,
    override val goal: Goal,
    optimizers: Set<Optimizer>,
    protected val migrationInterval: Int,
    protected val rng: Random = Random(System.currentTimeMillis()),
    override val monitor: Monitor<T>,
) : Engine<T>() {

    //Islands
    protected val islands: List<SimpleIsland<T>> = optimizers.map { SimpleIsland(problem, goal, it) }

    //Open Island
    protected val openIslands: List<SimpleIsland<T>> = islands.filter { it.isOpen }


    /**
     * Perform optimization
     */
    override fun optimize(): T {
        startTime = System.currentTimeMillis()

        do {
            itrCounter++

            islands.parallelStream().forEach { it.evaluate() }

            val min = islands.minByOrNull { it.bestFitness }!!
            if (min.bestFitness < bestFitness) {
                bestFitness = min.bestFitness
                bestSolution = min.bestSolution
            }

            if (itrCounter != 0L && itrCounter.rem(migrationInterval) == 0L)
                migrate()

            val solutions = islands.map { it.bestSolution }.toTypedArray()
            val fitness = islands.map { it.bestFitness }.toDoubleArray()

            monitor.debug(solutions, fitness)

            islands.parallelStream().forEach { it.iterate() }

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
}