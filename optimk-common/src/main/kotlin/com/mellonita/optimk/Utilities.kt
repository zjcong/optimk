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

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


public operator fun DoubleArray.plus(a: DoubleArray): DoubleArray =
    this.mapIndexed { index, d -> d + a[index] }.toDoubleArray()


public operator fun DoubleArray.minus(a: DoubleArray): DoubleArray =
    this.mapIndexed { index, d -> d - a[index] }.toDoubleArray()

public operator fun Double.times(a: DoubleArray): DoubleArray = a.map { it * this }.toDoubleArray()


/**
 *
 */
internal class SynchronizedProperty<T>(initValue: T) {
    private var value: T = initValue

    @Synchronized
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    @Synchronized
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}


/**
 *
 */
internal class InitOnceProperty<T> : ReadWriteProperty<Any, T> {

    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (value == EMPTY) {
            throw IllegalStateException("Value isn't initialized")
        } else {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (this.value != EMPTY) {
            throw IllegalStateException("Value is initialized")
        }
        this.value = value
    }
}

