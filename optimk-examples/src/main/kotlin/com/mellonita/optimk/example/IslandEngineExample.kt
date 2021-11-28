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

package com.mellonita.optimk.example

import com.mellonita.optimk.Engine
import com.mellonita.optimk.LogLevel
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.IslandEngine
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.monitor.DefaultMonitor
import com.mellonita.optimk.optimizer.DifferentialEvolution
import com.mellonita.optimk.Problem
import kotlin.random.Random


fun <T> getEngine(
    r: Int,
    p: Int,
    problem: Problem<T>
): Engine<T> {
    return DefaultEngine(
        problem = problem,
        monitor = object : DefaultMonitor<T>(LogLevel.INFO) {
            override fun stop(engine: Engine<T>): Boolean = false
        },
        optimizer = DifferentialEvolution(
            dimensionality = problem.dimensions,
            population = p,
            cr = 0.9,
            rng = Random(r),
            mutation = DifferentialEvolution.best1(0.8)
        )
    )
}

fun main() {
    val dimensionality = 30
    val population = 60
    val problem = Rastrigin(dimensionality)
    val maxIteration = 5_000L
    val islandNumber = 4

    val engine = IslandEngine(
        problem = problem,
        migrationInterval = 1,
        rng = Random(0),
        monitor = object : DefaultMonitor<DoubleArray>(LogLevel.INFO) {
            override fun stop(engine: Engine<DoubleArray>): Boolean {
                return engine.iterations >= maxIteration
            }
        },
        islands = (0 until islandNumber).map { getEngine(it, (population / islandNumber), problem) }
    )

    engine.optimize()
}