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

package com.mellonita.optimk.optimizer

import com.mellonita.optimk.Optimizer
import com.mellonita.optimk.optimizer.cmaes.HipparchusCMAES
import kotlin.random.Random


public class CovarianceMatrixAdaption(
    dimensionality: Int,
    population: Int,
    rng: Random = Random(0)
) : Optimizer(dimensionality, population, rng) {

    private var actualOptimizer =
        HipparchusCMAES(
            diagonalOnly = 10,
            lambda = population,
            dimension = dimensionality,
            random = rng
        )


    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {
        return actualOptimizer.iterate(population, fitness)
    }

    override fun initialize(): Array<DoubleArray> {
        actualOptimizer =
            HipparchusCMAES(
                diagonalOnly = 10,
                lambda = population,
                dimension = dimensionality,
                random = rng
            )
        return super.initialize()
    }

    override fun initialize(init: Array<DoubleArray>): Array<DoubleArray> {
        actualOptimizer =
            HipparchusCMAES(
                diagonalOnly = 10,
                lambda = population,
                dimension = dimensionality,
                random = rng
            )
        actualOptimizer.iterations = 0
        return super.initialize(init)
    }
}

