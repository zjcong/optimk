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

package com.mellonita.optimk.core.math

internal open class RealMatrix(arrays: Array<DoubleArray>) {

    private val rows: Int
    private val cols: Int
    private val isSquare: Boolean


    init {
        require(arrays.isNotEmpty()) { "Empty matrix" }
        require(arrays[0].isNotEmpty()) { "Empty row" }
        require(arrays.all { it.size == arrays[0].size }) { "Inconsistent rows" }

        rows = arrays.size
        cols = arrays[0].size
        isSquare = (rows == cols)
    }


    companion object {
        @Suppress("FunctionName")
        fun RealMatrix(rows: Int, cols: Int, init: (Int, Int) -> Double): RealMatrix {
            val a = Array(rows) { r -> DoubleArray(cols) { c -> init(r, c) } }
            return RealMatrix(a)
        }
    }
}