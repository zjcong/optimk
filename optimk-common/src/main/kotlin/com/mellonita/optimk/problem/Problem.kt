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

package com.mellonita.optimk.problem

import java.io.Serializable
import kotlin.math.roundToInt
import kotlin.math.roundToLong


/**
 * Problem Interface
 *
 */
public interface Problem<T> : Serializable {

    public val d: Int

    /**
     * Decode a vector of random keys into actual solution
     *
     * @param keys A vector of real number in range [0,1)
     * @return Actual solution
     */
    public fun decode(keys: DoubleArray): T

    /**
     * Objective function
     *
     * @param solution Candidate solution
     * @return Fitness of the candidate
     */
    public operator fun invoke(solution: T): Double

    /**
     * If a given solution is feasible
     * @param solution Candidate solution
     * @return Is feasible
     */
    public fun isFeasible(solution: T): Boolean = true

}

