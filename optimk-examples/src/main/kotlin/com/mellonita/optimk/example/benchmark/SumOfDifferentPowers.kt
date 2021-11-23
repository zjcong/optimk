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

package com.mellonita.optimk.example.benchmark

import com.mellonita.optimk.valueIn
import kotlin.math.abs
import kotlin.math.pow

class SumOfDifferentPowers(d: Int) : Benchmark(d) {
    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.map { it.valueIn((-1.0).rangeTo(1.0)) }.toDoubleArray()
    }

    override fun objective(solution: DoubleArray): Double {
        return solution.withIndex().sumOf {
            abs(it.value).pow(it.index + 1)
        }
    }

}