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

class ATT48 : TSP(48) {

    override val distanceMatrix: Array<IntArray>
    override val globalMinima: Long = 33523L

    init {
        val contents = javaClass.getResource("/tsp/att48_d.txt")!!.readText().trim()
        val rows = contents.split("\n")
        distanceMatrix = Array(d) { r ->
            rows[r].trim().split(regex = "\\s+".toRegex()).map { it.toInt() }.toIntArray()
        }
    }
}
