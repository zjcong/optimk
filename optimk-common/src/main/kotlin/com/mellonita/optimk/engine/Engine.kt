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

import com.mellonita.optimk.monitor.Monitor
import com.mellonita.optimk.problem.Problem
import java.io.*


/**
 * Goal Type
 */
public enum class Goal(private val value: Int) : Serializable {
    Maximize(-1),
    Minimize(1);

    public operator fun times(d: Double): Double = this.value.toDouble() * d
}

/**
 * Engine
 */
public abstract class Engine<T> : Serializable {


    public abstract val problem: Problem<T>
    public abstract val goal: Goal
    public abstract val monitor: Monitor<T>
    public abstract val isOpen: Boolean

    public var bestSolution: DoubleArray = doubleArrayOf()
        protected set
    public var bestFitness: Double = Double.MAX_VALUE
        protected set

    public var evaluations: Long = 0L
        protected set
    public var startTime: Long = System.currentTimeMillis()
        protected set
    public var iterations: Long = 0
        protected set

    protected fun debug(msg: String): Unit = monitor.debug(this, msg)

    /**
     * Single iteration
     */
    public abstract fun updateFitness()

    /**
     *
     */
    public abstract fun nextIteration()


    /**
     *
     */
    public abstract fun arrival(s: DoubleArray, f: Double): Boolean

    /**
     * Perform optimization
     */
    public abstract fun optimize(): T

    /**
     * Single objective function evaluation
     */
    protected open fun evaluateIndividual(solution: DoubleArray): Double {
        evaluations++
        if (solution.any { it !in (0.0).rangeTo(1.0) })
            return Double.MAX_VALUE
        val actualCandidate = problem.decode(solution)
        return if (problem.isFeasible(actualCandidate))
            goal * problem.objective(actualCandidate)
        else
            Double.MAX_VALUE
    }


    /**
     * Serialize this engine to file
     */
    public fun suspendTo(file: File) {
        val fos = FileOutputStream(file)
        val oos = ObjectOutputStream(fos)
        oos.writeObject(this)
        oos.close()
        fos.close()
        debug("Engine suspended to [${file.name}]")
    }


    public companion object {

        /**
         * Deserialize from file
         */
        @Suppress("UNCHECKED_CAST")
        public fun <T> resumeFrom(f: File): T {
            val fis = FileInputStream(f)
            val ois = ObjectInputStream(fis)
            val engine = ois.readObject()
            fis.close()
            ois.close()
            return engine as T
        }
    }
}

