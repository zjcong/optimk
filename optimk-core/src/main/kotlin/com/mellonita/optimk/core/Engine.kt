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

package com.mellonita.optimk.core

import java.io.*


/**
 * Island Interface
 */
public interface Island : Serializable {

    /**
     * Upon immigrant arrival
     */
    public fun arrival(s: DoubleArray, f: Double): Boolean
}


/**
 * Engine
 */
public abstract class Engine<T> : Serializable, Island {

    public abstract val name: String

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
    public var startTime: Long = 0
        protected set

    /**
     * Number of iterations
     */
    public var iterations: Long = 0
        protected set

    /**
     * Wrapper of log
     */
    protected fun log(level: LogLevel, msg: String): Unit = monitor.log(level, this, msg)
    protected fun debug(msg: String): Unit = log(LogLevel.DEBUG, msg)
    protected fun info(msg: String): Unit = log(LogLevel.INFO, msg)
    protected fun warn(msg: String): Unit = log(LogLevel.WARN, msg)
    protected fun error(msg: String): Unit = log(LogLevel.ERROR, msg)

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
    protected open fun evaluatePopulation(batchKeys: Array<DoubleArray>): DoubleArray {
        return batchKeys.map { evaluateIndividual(it) }.toDoubleArray()
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
        info("Engine suspended to [${file.name}]")
    }

    /**
     * Companion functions
     */
    public companion object {

        /**
         * Deserialize from file
         */
        @Suppress("UNCHECKED_CAST")
        public fun <T> resumeFrom(f: File): Engine<T> {
            val fis = FileInputStream(f)
            val ois = ObjectInputStream(fis)
            val engine = ois.readObject() as Engine<*>
            fis.close()
            ois.close()
            return engine as Engine<T>
        }
    }
}

