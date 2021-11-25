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


package com.mellonita.optimk

/**
 *
 */
public operator fun DoubleArray.plus(a: DoubleArray): DoubleArray {
    require(a.size == this.size) { "${this.size} != ${a.size}" }
    return this.mapIndexed { index, d -> d + a[index] }.toDoubleArray()
}

/**
 *
 */
public operator fun DoubleArray.minus(a: DoubleArray): DoubleArray =
    this.mapIndexed { index, d -> d - a[index] }.toDoubleArray()

/**
 *
 */
public operator fun Double.times(a: DoubleArray): DoubleArray = a.map { it * this }.toDoubleArray()

