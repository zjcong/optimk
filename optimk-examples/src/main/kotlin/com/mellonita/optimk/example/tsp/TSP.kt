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

package com.mellonita.optimk.example.tsp

import com.mellonita.optimk.core.Goal
import com.mellonita.optimk.core.Problem

abstract class TSP(override val d: Int) : Problem<IntArray> {

    override val goal: Goal = Goal.Minimize

    abstract val distanceMatrix: Array<IntArray>

    abstract val globalMinima: Long

    override fun objective(solution: IntArray): Double {
        return solution.indices.sumOf {
            val from = solution[it]
            val to = if (it == (solution.size - 1)) solution[0] else solution[it + 1]
            distanceMatrix[from][to]
        }.toDouble() - this.globalMinima.toDouble()
    }

    override fun decode(keys: DoubleArray): IntArray {
        val p = keys.withIndex().sortedBy { it.value }.map { it.index }.toIntArray()
        return p
    }
}