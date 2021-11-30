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
import com.mellonita.optimk.core.engine.DefaultEngine
import com.mellonita.optimk.core.monitor.DefaultMonitor
import com.mellonita.optimk.core.sampler.DifferentialEvolution
import com.mellonita.optimk.example.benchmark.SumOfDifferentPowers
import java.io.File
import kotlin.random.Random


fun main() {


    val engine = DefaultEngine(
        name = "Default DE",
        problem = SumOfDifferentPowers(10),
        sampler = DifferentialEvolution(
            d = 10,
            p = 20,
            rng = Random(0)
        ),
        monitor = object : DefaultMonitor<DoubleArray>(LogLevel.INFO) {

            override fun stop(engine: Engine<DoubleArray>): Boolean {
                //Save state every 5 iterations
                if (engine.iterations.rem(5L) == 0L)
                    engine.suspendTo(File("suspended_engine.bin"))

                if (engine.bestFitness >= 1E-5) return false
                return true
            }
        }
    )

    val result1 = engine.optimize()

    // resume from the saved state
    val resumedEngine =
        Engine.resumeFrom<DoubleArray>(File("suspended_engine.bin"))
    val result2 = resumedEngine.optimize()

    assert(result1.contentEquals(result2))
}