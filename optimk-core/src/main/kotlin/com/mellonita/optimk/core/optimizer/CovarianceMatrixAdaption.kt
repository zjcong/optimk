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

@file:Suppress("SpellCheckingInspection", "KDocUnresolvedReference")

package com.mellonita.optimk.core.optimizer

import com.mellonita.optimk.core.Optimizer
import com.mellonita.optimk.core.optimizer.cmaes.InriaCMAES
import kotlin.random.Random


public class CovarianceMatrixAdaption(
    d: Int,
    p: Int,
    rng: Random = Random(0)
) : Optimizer(d, p, rng) {


    private var inriaCMAES = InriaCMAES(d, p, rng)


    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {
        return inriaCMAES.iterate(population, fitness)
    }

    override fun initialize(): Array<DoubleArray> {
        return inriaCMAES.initialize()
    }

    override fun initialize(init: Array<DoubleArray>): Array<DoubleArray> {
        return inriaCMAES.initialize()
    }
}

