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
import com.mellonita.optimk.core.Monitor
import com.mellonita.optimk.core.math.valueIn
import com.mellonita.optimk.core.monitor.DefaultMonitor


/**
 *
 */
abstract class SingleObjectiveCOCOProblem(
    val cocoProblem: Problem,
    private val maxEvalMultiplier: Long
) : com.mellonita.optimk.core.Problem<DoubleArray> {

    override val d: Int = cocoProblem.dimension
    override val goal: Goal = Goal.Minimize

    init {
        //println("problemid: ${cocoProblem.id}")
    }

    fun getMonitor(): Monitor<DoubleArray> = COCOMonitor(cocoProblem, d * maxEvalMultiplier)

    override fun objective(solution: DoubleArray): Double {
        return cocoProblem.evaluateFunction(solution)[0]
    }

    private class COCOMonitor(val p: Problem, val maxEvaluations: Long) : DefaultMonitor<DoubleArray>(LOGLEVEL) {
        override fun stop(engine: Engine<DoubleArray>): Boolean {
            if (p.isFinalTargetHit) print("@")
            return (p.evaluations >= maxEvaluations || p.isFinalTargetHit)
        }

    }
}


/**
 * Continuous problem
 */
internal class ContinuousProblem(
    cocoProblem: Problem,
    maxEvalMultiplier: Long
) : SingleObjectiveCOCOProblem(cocoProblem, maxEvalMultiplier) {

    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.indices.map { keys[it].valueIn((-5.0).rangeTo(5.0)) }.toDoubleArray()
    }


}


/**
 * Mix Int problem
 */
internal class MixIntProblem(
    cocoProblem: Problem,
    maxEvalMultiplier: Long
) : SingleObjectiveCOCOProblem(cocoProblem, maxEvalMultiplier) {

    override fun decode(keys: DoubleArray): DoubleArray {
        require(keys.size.rem(5) == 0)

        val solution = DoubleArray(keys.size)
        val p = keys.size / 5

        (0 until 5).forEach { pi ->

            (0 until p).forEach {
                val si = pi * p + it
                val v = when (pi) {
                    0 -> keys[si].valueIn(0..1).toDouble()
                    1 -> keys[si].valueIn(0..3).toDouble()
                    2 -> keys[si].valueIn(0..7).toDouble()
                    3 -> keys[si].valueIn(0..15).toDouble()
                    4 -> keys[si].valueIn((-5.0).rangeTo(5.0))
                    else -> {
                        throw RuntimeException("This should not happen")
                    }
                }
                solution[si] = v
            }
        }
        return solution
    }


}


