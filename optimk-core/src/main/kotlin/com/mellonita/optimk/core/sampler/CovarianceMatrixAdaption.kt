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

package com.mellonita.optimk.core.sampler

import com.mellonita.optimk.core.Sampler
import fr.inria.optimization.cmaes.CMAEvolutionStrategy
import kotlin.random.Random


public class CovarianceMatrixAdaption(d: Int, p: Int, rng: Random) : Sampler(d, p, rng) {

    private var cma: CMAEvolutionStrategy = CMAEvolutionStrategy()

    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {
        if ((cma.stopConditions.number != 0)) return population

        cma.updateDistribution(fitness.copyOfRange(0, populationSize - 1))

        val sampled = cma.samplePopulation()
        return sampled.plusElement(DoubleArray(dimensions) { di -> sampled.sumOf { it[di] } / sampled.size.toDouble() })
    }

    private fun inriaCMAESInit() {
        cma = CMAEvolutionStrategy()
        cma.dimension = dimensions
        cma.setInitialX(0.65)
        cma.setInitialStandardDeviation(0.01)
        cma.parameters.populationSize = populationSize - 1
        cma.options.stopTolFunHist = 1e-13
        cma.seed = rng.nextLong()
        cma.options.verbosity = -2
    }

    override fun initialize(): Array<DoubleArray> {
        inriaCMAESInit()
        cma.init()
        return cma.samplePopulation().plusElement(DoubleArray(dimensions) { rng.nextDouble() })
    }

    override fun initialize(init: Array<DoubleArray>): Array<DoubleArray> {
        inriaCMAESInit()
        cma.initialX = init[0]
        cma.init()
        return cma.samplePopulation().plusElement(DoubleArray(dimensions) { rng.nextDouble() })
    }
}