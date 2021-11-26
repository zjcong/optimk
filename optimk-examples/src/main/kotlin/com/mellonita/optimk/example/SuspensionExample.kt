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
import com.mellonita.optimk.Goal
import com.mellonita.optimk.LogLevel
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.example.benchmark.SumOfDifferentPowers
import com.mellonita.optimk.monitor.DefaultMonitor
import com.mellonita.optimk.optimizer.DifferentialEvolution
import java.io.File


fun main() {


    val engine = DefaultEngine(
        problem = SumOfDifferentPowers(10),
        goal = Goal.Minimize,
        optimizer = DifferentialEvolution(
            dimensionality = 10,
            population = 20,
            mutation = DifferentialEvolution.rand1(0.7)
        ),
        monitor = object : DefaultMonitor<DoubleArray>(LogLevel.INFO) {

            override fun stop(engine: Engine<DoubleArray>): Boolean {
                //Save state every 100 iterations
                if (engine.iterations.rem(100L) == 0L)
                    engine.suspendTo(File("suspended_engine.bin"))

                if (engine.bestFitness >= 1E-5) return false
                return true
            }
        }
    )

    val result1 = engine.optimize()

    // resume from the saved state
    val resumedEngine = Engine.resumeFrom<DefaultEngine<DoubleArray>>(File("suspended_engine.bin"))
    val result2 = resumedEngine.optimize()

    assert(result1.contentEquals(result2))
}