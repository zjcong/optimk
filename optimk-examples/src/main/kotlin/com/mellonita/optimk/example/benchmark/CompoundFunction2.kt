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

import kotlin.math.max
import kotlin.math.min


class CompoundFunction2(dimensions: Int) : Benchmark(dimensions) {

    private val f1 = Rastrigin(dimensions)
    private val f2 = Rosenbrock(dimensions)

    private val f1Scale = 0.02

    override val upperBound: Double = max(f1.upperBound, f2.upperBound)
    override val lowerBound: Double = min(f1.lowerBound, f2.lowerBound)
    override val globalOptima: Double = f1.globalOptima * f1Scale + f2.globalOptima


    override fun objective(solution: DoubleArray): Double {
        return f1.objective(solution) * f1Scale + f2.objective(solution)
    }
}
/*
fun main() {
    Rastrigin::class.plot()
    CompoundFunction2::class.plot()
}
*/