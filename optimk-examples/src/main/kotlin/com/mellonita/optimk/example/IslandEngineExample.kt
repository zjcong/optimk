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

import com.mellonita.optimk.core.Engine
import com.mellonita.optimk.core.LogLevel
import com.mellonita.optimk.core.Problem
import com.mellonita.optimk.core.engine.DefaultEngine
import com.mellonita.optimk.core.engine.IslandEngine
import com.mellonita.optimk.core.monitor.DefaultMonitor
import com.mellonita.optimk.core.sampler.CovarianceMatrixAdaption
import com.mellonita.optimk.example.benchmark.Rastrigin
import kotlin.random.Random


fun <T> getEngine(
    name: String,
    r: Int,
    p: Int,
    problem: Problem<T>
): Engine<T> {
    return DefaultEngine(
        name = name,
        problem = problem,
        monitor = object : DefaultMonitor<T>(LogLevel.INFO) {
            override fun stop(engine: Engine<T>): Boolean = false
        },
        sampler = CovarianceMatrixAdaption(
            d = problem.d,
            p = p,
            rng = Random(r),
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
        name = "Island",
        problem = problem,
        rng = Random(0),
        monitor = object : DefaultMonitor<DoubleArray>(LogLevel.INFO) {
            override fun stop(engine: Engine<DoubleArray>): Boolean {
                return engine.iterations >= maxIteration
            }
        },
        islands = (0 until islandNumber).map { getEngine("Island-$it", it, (population / islandNumber), problem) }
    )
    engine.optimize()
}