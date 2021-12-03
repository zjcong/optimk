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

package com.mellonita.optimk.coco

import Problem
import com.mellonita.optimk.core.Engine
import com.mellonita.optimk.core.Goal
import com.mellonita.optimk.core.LogLevel
import com.mellonita.optimk.core.Monitor
import com.mellonita.optimk.core.math.valueIn
import com.mellonita.optimk.core.monitor.DefaultMonitor


/**
 * A wrapper class of COCO Problem
 */
internal class SingleObjCOCOProblem(private val cocoProblem: Problem) :
    com.mellonita.optimk.core.Problem<DoubleArray> {
    override val d: Int = cocoProblem.dimension
    override val goal: Goal = Goal.Minimize

    override fun objective(solution: DoubleArray): Double {
        return cocoProblem.evaluateFunction(solution)[0]
    }

    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.indices.map {
            keys[it].valueIn(
                (cocoProblem.smallestValuesOfInterest[it]).rangeTo(cocoProblem.largestValuesOfInterest[it])
            )
        }.toDoubleArray()
    }

    fun getMonitor(): Monitor<DoubleArray> {
        return object : DefaultMonitor<DoubleArray>(LogLevel.WARN) {

            override fun stop(engine: Engine<DoubleArray>): Boolean {
                if (cocoProblem.isFinalTargetHit) print("@")
                return (engine.iterations >= maxItr || cocoProblem.isFinalTargetHit)
            }
        }
    }
}

