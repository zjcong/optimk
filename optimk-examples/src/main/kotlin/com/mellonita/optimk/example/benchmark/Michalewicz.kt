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
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class Michalewicz(d: Int) : Benchmark(d) {

    fun f(x: DoubleArray): Double {
        var sum = 0.0
        for (i in x.indices) {
            sum -= sin(x[i]) * sin((i + 1) * x[i].pow(2.0) / Math.PI).pow(20.0)
        }
        return sum
    }

    override fun decode(keys: DoubleArray): DoubleArray {
        return keys.map { it.valueIn(0.0.rangeTo(PI)) }.toDoubleArray()
    }

    override fun invoke(solution: DoubleArray): Double {
        return f(solution)
    }
}

fun main() {
    val f = Michalewicz(2)
    println(f(doubleArrayOf(2.20290552014618, 1.57079632677565)))
}