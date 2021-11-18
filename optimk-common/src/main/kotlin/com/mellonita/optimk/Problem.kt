package com.mellonita.optimk

import kotlin.math.roundToInt
import kotlin.math.roundToLong


/**
 * Problem Interface
 *
 */
interface Problem<T> {

    /**
     * Decode a vector of random keys into actual solution
     *
     * @param keys A vector of real number in range [0,1)
     * @return Actual solution
     */
    fun decode(keys: DoubleArray): T

    /**
     * Objective function
     *
     * @param candidate Candidate solution
     * @return Fitness of the candidate
     */
    fun objective(candidate: T): Double

    /**
     * If a given solution is feasible
     * @param candidate Candidate solution
     * @return Is feasible
     */
    fun isFeasible(candidate: T): Boolean = true

}


/**
 * Maps a double value to a value of a given double range
 * @param range Range
 * @return value
 */
fun Double.valueIn(range: ClosedFloatingPointRange<Double>): Double {
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
fun <T> Double.elementIn(list: List<T>): T = list[this.valueIn(list.indices)]


/**
 * Maps a double value to a value in a integer range
 */
fun Double.valueIn(range: IntRange): Int =
    this.valueIn(range.first.toDouble().rangeTo(range.last.toDouble())).roundToInt()


/**
 * Maps a double value to a value of a Long range
 * @param range Range of long
 * @return value
 */
fun Double.valueIn(range: LongRange): Long =
    this.valueIn(range.first.toDouble().rangeTo(range.last.toDouble())).roundToLong()
