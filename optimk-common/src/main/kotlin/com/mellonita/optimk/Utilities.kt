package com.mellonita.optimk

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


operator fun DoubleArray.plus(a: DoubleArray): DoubleArray {
    return this.mapIndexed { index, d -> d + a[index] }.toDoubleArray()
}

operator fun DoubleArray.minus(a: DoubleArray): DoubleArray {
    return this.mapIndexed { index, d -> d - a[index] }.toDoubleArray()
}

operator fun Double.times(a: DoubleArray): DoubleArray {
    return a.map { it * this }.toDoubleArray()
}

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

