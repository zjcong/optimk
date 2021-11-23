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


@file:Suppress("MemberVisibilityCanBePrivate")

package com.mellonita.optimk.engine

import com.mellonita.optimk.InitOnceProperty
import com.mellonita.optimk.Monitor
import com.mellonita.optimk.Problem

/**
 * Goal Type
 */
public enum class Goal(private val value: Int) {
    Maximize(-1),
    Minimize(1);

    public operator fun times(d: Double): Double = this.value.toDouble() * d
}

/**
 * Engine
 */
public abstract class Engine<T> {

    protected abstract val monitor: Monitor<T>

    public abstract val problem: Problem<T>
    public abstract val goal: Goal

    public var bestSolution: DoubleArray = doubleArrayOf()
    public var bestFitness: Double = Double.MAX_VALUE

    public var evalCounter: Long = 0L
    public var startTime: Long by InitOnceProperty()
    public var itrCounter: Long = 0


    /**
     * Perform optimization
     */
    public abstract fun optimize(): T

    /**
     * Single objective function evaluation
     */
    public open fun evaluateIndividual(candidate: DoubleArray): Double {
        evalCounter++
        if (candidate.any { it !in (0.0).rangeTo(1.0) })
            return Double.MAX_VALUE
        val actualCandidate = problem.decode(candidate)
        return if (problem.isFeasible(actualCandidate))
            goal * problem.objective(actualCandidate)
        else
            Double.MAX_VALUE
    }
}