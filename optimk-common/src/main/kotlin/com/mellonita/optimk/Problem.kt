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

import java.io.Serializable


public enum class Goal(public val value: Double) {
    Maximize(-1.0), Minimize(1.0);

    public operator fun times(fitness: Double): Double = value * fitness
}

/**
 * Problem Interface
 *
 */
public interface Problem<T> : Serializable {

    /**
     * Number of dimensions of this problem
     */
    public val d: Int

    /**
     * Goal of this problem
     */
    public val goal: Goal

    /**
     * Objective function
     *
     * @param solution Candidate solution
     * @return Fitness of the candidate
     */
    public fun objective(solution: T): Double

    /**
     * Decode a vector of random keys into actual solution
     *
     * @param keys A vector of real number in range [0,1)
     * @return Actual solution
     */
    public fun decode(keys: DoubleArray): T

    /**
     * If a given solution is feasible
     * @param solution Candidate solution
     * @return Is feasible
     */
    public fun isFeasible(solution: T): Boolean = true

    /**
     * Get fitness value of a solution * goal
     */
    public operator fun invoke(keys: DoubleArray): Double {
        val solution = decode(keys)
        if (!isFeasible(solution)) return Double.MAX_VALUE
        return goal * objective(solution)
    }

    /**
     * Get fitness values of a set of solutions
     */
    public operator fun invoke(batchKeys: Array<DoubleArray>): DoubleArray {
        return (batchKeys.indices).map { invoke(batchKeys[it]) }.toDoubleArray()
    }
}

