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

package com.mellonita.optimk.core.problem

import com.mellonita.optimk.core.Problem


/**
 *
 */
public interface ParallelProblem<T> : Problem<T> {

    public override operator fun invoke(batchKeys: Array<DoubleArray>): DoubleArray {
        return (batchKeys.indices)
            .toList()
            .parallelStream()
            .mapToDouble {
                if (batchKeys[it].any { di -> di < 0.0 || di > 1.0 }) Double.MAX_VALUE
                else {
                    val solution = decode(batchKeys[it])
                    if (!isFeasible(solution)) Double.MAX_VALUE
                    else goal * objective(solution)
                }
            }
            .toArray()
    }
}