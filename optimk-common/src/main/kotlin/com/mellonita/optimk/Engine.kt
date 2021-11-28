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

package com.mellonita.optimk

import java.io.*


/**
 * Island Interface
 */
public interface Island : Serializable {
    /**
     * If this island is open
     */
    public val isOpen: Boolean

    /**
     * Upon immigrant arrival
     */
    public fun arrival(s: DoubleArray, f: Double): Boolean
}


/**
 * Engine
 */
public abstract class Engine<T> : Serializable, Island {

    /**
     * The problem
     */
    public abstract val problem: Problem<T>

    /**
     * Monitor
     */
    public abstract val monitor: Monitor<T>

    /**
     * Best solution of the current generation
     */
    public var bestSolution: DoubleArray = doubleArrayOf()
        protected set

    /**
     * Fitness of the best solutions
     */
    public var bestFitness: Double = Double.MAX_VALUE
        protected set

    /**
     * Number of evaluations
     */
    public var evaluations: Long = 0L
        protected set

    /**
     * Start time
     */
    public var startTime: Long = System.currentTimeMillis()
        protected set

    /**
     * Number of iterations
     */
    public var iterations: Long = 0
        protected set

    /**
     * Wrapper of debug log
     */
    protected fun log(level: LogLevel, msg: String): Unit = monitor.log(level, this, msg)

    /**
     * Single iteration
     */
    public abstract fun updateFitness()

    /**
     * Perform next iteration of sampling
     */
    public abstract fun nextIteration()

    /**
     * Perform batch optimization
     */
    public abstract fun optimize(): T

    /**
     * Single objective function evaluation
     */
    protected open fun evaluateIndividual(keys: DoubleArray): Double {
        evaluations++
        if (keys.any { it !in (0.0).rangeTo(1.0) }) return Double.MAX_VALUE
        val f = problem(keys)
        if (f.isNaN()) throw RuntimeException("Solution: ${problem.decode(keys)} yields NaN value")
        return f
    }

    /**
     *
     */
    protected open fun evaluatePopulation(batchKeys: Array<DoubleArray>): DoubleArray =
        batchKeys.indices.map { evaluateIndividual(batchKeys[it]) }.toDoubleArray()

    /**
     * Serialize this engine to file
     */
    public fun suspendTo(file: File) {
        val fos = FileOutputStream(file)
        val oos = ObjectOutputStream(fos)
        oos.writeObject(this)
        oos.close()
        fos.close()
        log(LogLevel.INFO, "Engine suspended to [${file.name}]")
    }

    /**
     * Companion functions
     */
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

