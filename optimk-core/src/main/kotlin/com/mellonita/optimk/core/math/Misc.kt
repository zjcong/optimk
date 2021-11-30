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


package com.mellonita.optimk.core.math

import kotlin.math.*
import kotlin.random.Random

/**
 * Vector plus
 */
public operator fun DoubleArray.plus(a: DoubleArray): DoubleArray {
    require(a.size == this.size) { "${this.size} != ${a.size}" }
    return DoubleArray(a.size) { this[it] + a[it] }
}

/**
 * Vector minus
 */
public operator fun DoubleArray.minus(a: DoubleArray): DoubleArray {
    require(a.size == this.size) { "${this.size} != ${a.size}" }
    return DoubleArray(a.size) { this[it] - a[it] }
}

/**
 * Scalar multiply vector
 */
public operator fun Double.times(a: DoubleArray): DoubleArray = DoubleArray(a.size) { a[it] * this }


/**
 * Maps a double value to a value of a given double range
 * @param range Range
 * @return value
 */
public fun Double.valueIn(range: ClosedFloatingPointRange<Double>): Double {
    val r = range.endInclusive - range.start
    require(r.isFinite()) { "Infinite range" }
    require(!r.isNaN()) { "Invalid range" }
    return range.start + r * this
}


/**
 * Maps a double value to an element of a given list
 * @param list List
 * @return element
 */
public fun <T> Double.elementIn(list: List<T>): T = list[this.valueIn(list.indices)]


/**
 * Maps a double value to a value in a integer range
 * @param range Integer range
 * @return Integer
 */
public fun Double.valueIn(range: IntRange): Int =
    this.valueIn(range.first.toDouble().rangeTo(range.last.toDouble())).roundToInt()


/**
 * Maps a double value to a value of a Long range
 * @param range Range of long
 * @return value
 */
public fun Double.valueIn(range: LongRange): Long =
    this.valueIn(range.first.toDouble().rangeTo(range.last.toDouble())).roundToLong()

/**
 *
 */
public fun Random.nextGaussian(): Double {
    return inverseStdNormalCDF(this.nextDouble())
}

/**
 * Inverse std normal CDF
 */
internal fun inverseStdNormalCDF(probability: Double): Double {
    val invP1 = doubleArrayOf(
        0.160304955844066229311E2,
        -0.90784959262960326650E2,
        0.18644914861620987391E3,
        -0.16900142734642382420E3,
        0.6545466284794487048E2,
        -0.864213011587247794E1,
        0.1760587821390590
    )
    val invQ1 = doubleArrayOf(
        0.147806470715138316110E2,
        -0.91374167024260313396E2,
        0.21015790486205317714E3,
        -0.22210254121855132366E3,
        0.10760453916055123830E3,
        -0.206010730328265443E2,
        0.1E1
    )
    val invP2 = doubleArrayOf(
        -0.152389263440726128E-1,
        0.3444556924136125216,
        -0.29344398672542478687E1,
        0.11763505705217827302E2,
        -0.22655292823101104193E2,
        0.19121334396580330163E2,
        -0.5478927619598318769E1,
        0.237516689024448000
    )
    val invQ2 = doubleArrayOf(
        -0.108465169602059954E-1,
        0.2610628885843078511,
        -0.24068318104393757995E1,
        0.10695129973387014469E2,
        -0.23716715521596581025E2,
        0.24640158943917284883E2,
        -0.10014376349783070835E2,
        0.1E1
    )
    val invP3 = doubleArrayOf(
        0.56451977709864482298E-4,
        0.53504147487893013765E-2,
        0.12969550099727352403,
        0.10426158549298266122E1,
        0.28302677901754489974E1,
        0.26255672879448072726E1,
        0.20789742630174917228E1,
        0.72718806231556811306,
        0.66816807711804989575E-1,
        -0.17791004575111759979E-1,
        0.22419563223346345828E-2
    )
    val invQ3 = doubleArrayOf(
        0.56451699862760651514E-4,
        0.53505587067930653953E-2,
        0.12986615416911646934,
        0.10542932232626491195E1,
        0.30379331173522206237E1,
        0.37631168536405028901E1,
        0.38782858277042011263E1,
        0.20372431817412177929E1,
        0.1E1
    )

    var i: Int
    val negatif: Boolean
    var y: Double
    var z: Double
    var v: Double
    var w: Double
    var x = probability
    if (probability <= 0.0) return Double.NEGATIVE_INFINITY
    if (probability >= 1.0) return Double.POSITIVE_INFINITY

    // Transform x as argument of InvErf
    x = 2.0 * x - 1.0
    if (x < 0.0) {
        x = -x
        negatif = true
    } else negatif = false
    if (x <= 0.75) {
        y = x * x - 0.5625
        w = 0.0
        v = w
        i = 6
        while (i >= 0) {
            v = v * y + invP1[i]
            w = w * y + invQ1[i]
            i--
        }
        z = v / w * x
    } else if (x <= 0.9375) {
        y = x * x - 0.87890625
        w = 0.0
        v = w
        i = 7
        while (i >= 0) {
            v = v * y + invP2[i]
            w = w * y + invQ2[i]
            i--
        }
        z = v / w * x
    } else {
        y = if (probability > 0.5) 1.0 / sqrt(-ln(1.0 - x)) else 1.0 / sqrt(-ln(2.0 * probability))
        v = 0.0
        i = 10
        while (i >= 0) {
            v = v * y + invP3[i]
            i--
        }
        w = 0.0
        i = 8
        while (i >= 0) {
            w = w * y + invQ3[i]
            i--
        }
        z = v / w / y
    }
    return if (negatif) {
        if (probability < 1.0e-105) {
            val constant = 1.77245385090551602729
            w = exp(-z * z) / constant // pdf
            y = 2.0 * z * z
            v = 1.0
            var term = 1.0
            // Asymptotic series for erfc(z) (apart from exp factor)
            i = 0
            while (i < 6) {
                term *= -(2 * i + 1) / y
                v += term
                ++i
            }
            // Apply 1 iteration of Newton solver to get last few decimals
            z -= probability / w - 0.5 * v / z
        }
        -(z * 1.41421356237309504880)
    } else z * 1.41421356237309504880
}
