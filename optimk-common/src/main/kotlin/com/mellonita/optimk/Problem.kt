package com.mellonita.optimk

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
    public fun objective(solution: T): Double

    /**
     * If a given solution is feasible
     * @param solution Candidate solution
     * @return Is feasible
     */
    public fun isFeasible(solution: T): Boolean = true

}


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
