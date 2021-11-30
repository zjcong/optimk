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

package com.mellonita.optimk.core.sampler

import com.mellonita.optimk.core.OpenBorder
import com.mellonita.optimk.core.Sampler
import com.mellonita.optimk.core.math.nextGaussian
import com.mellonita.optimk.core.math.plus
import kotlin.math.sqrt
import kotlin.random.Random

@Suppress("UNUSED_PARAMETER")
public class RandomSampler(
    d: Int,
    p: Int,
    private val sigma: Double = 1E-10,
    rng: Random = Random(0)
) : Sampler(d, p, rng), OpenBorder {

    public constructor(d: Int, p: Int, rng: Random) : this(d, p, 1E-10, rng)

    override fun iterate(population: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {
        var bi = 0
        var wi = 0
        fitness.indices.forEach { i ->
            if (fitness[i] > fitness[wi]) wi = i
            if (fitness[i] < fitness[bi]) bi = i
        }
        val best = population[bi]
        //val worst = population[wi]

        return Array(this.p) {
            if (it == 0) {
                best
            } else {
                marsaglia(best, sigma)
                //DoubleArray(d) { di -> best[di] + rng.nextDouble(ranges[di]) - ranges[di] }
            }
        }
    }

    private fun marsaglia(center: DoubleArray, sigma: Double): DoubleArray {
        val x = DoubleArray(d) { rng.nextGaussian() * sigma }
        val r = sqrt(x.sum())
        val rv = x.map { it / r }.toDoubleArray()
        return center.plus(rv)
    }
}