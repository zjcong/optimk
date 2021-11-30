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

package com.mellonita.optimk.core.optimizer.cmaes

import com.mellonita.optimk.core.Optimizer
import fr.inria.optimization.cmaes.CMAEvolutionStrategy
import kotlin.random.Random

public class InriaCMAES(d: Int, p: Int, rng: Random) : Optimizer(d, p, rng) {
    private val cma: CMAEvolutionStrategy = CMAEvolutionStrategy()

    private var fitness: DoubleArray

    init {
        cma.dimension = d
        cma.setInitialX(0.5)
        cma.setInitialStandardDeviation(0.05)
        cma.parameters.populationSize = p
        cma.options.stopTolFunHist = 1e-13
        cma.seed = rng.nextLong()
        fitness = cma.init()
    }

    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {
        if ((cma.stopConditions.number != 0)) return population
        cma.updateDistribution(fitness)
        return cma.samplePopulation()
    }

    override fun initialize(): Array<DoubleArray> {
        return cma.samplePopulation()
    }
}