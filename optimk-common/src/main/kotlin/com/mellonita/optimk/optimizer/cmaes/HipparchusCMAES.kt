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

package com.mellonita.optimk.optimizer.cmaes

import org.hipparchus.exception.LocalizedCoreFormats
import org.hipparchus.exception.MathIllegalArgumentException
import org.hipparchus.linear.Array2DRowRealMatrix
import org.hipparchus.linear.EigenDecomposition
import org.hipparchus.linear.MatrixUtils
import org.hipparchus.linear.RealMatrix
import org.hipparchus.optim.PointValuePair
import org.hipparchus.util.FastMath
import java.util.*

class HipparchusCMAES(
    private var diagonalOnly: Int,
    private val random: Random,
    private val dimension: Int,
    private val inputSigma: DoubleArray = DoubleArray(dimension) { 0.3 },
    private val lambda: Int
) {


    private val checkFeasibleCount: Int = 2
    private val maxIterations = Int.MAX_VALUE
    private val stopFitness = 0.0
    private val isActiveCMA = true
    private var isMinimize = true

    private var converged = false
    private var bestValue: Double? = null
    private var optimum: PointValuePair? = null
    private var lastResult: PointValuePair? = null
    private val fitfun: FitnessFunction = FitnessFunction()

    private val arz = randn1(dimension, lambda)
    private val arx = zeros(dimension, lambda)


    fun iterate(p: Array<DoubleArray>, fitness: DoubleArray): Array<DoubleArray> {
        if (converged) return p
        if (iterations == 0) {
            val b = fitness.withIndex().minByOrNull { it.value }!!
            initializeCMA(p[b.index])
            bestValue = b.value
            push(fitnessHistory, bestValue!!)
            optimum = PointValuePair(p[0], bestValue!!)
            iterations++
        } else {
            // Sort by fitness and compute weighted mean into xmean
            val arindex = sortedIndices(fitness)
            // Calculate new xmean, this is selection and recombination
            val xold = xmean // for speed up of Eq. (2) and (3)
            val bestArx = selectColumns(arx, arindex.copyOf(mu))
            xmean = bestArx.multiply(weights)
            val bestArz = selectColumns(arz, arindex.copyOf(mu))
            val zmean = bestArz.multiply(weights)
            val hsig = updateEvolutionPaths(zmean, xold)
            if (diagonalOnly <= 0) {
                updateCovariance(hsig, bestArx, arz, arindex, xold)
            } else {
                updateCovarianceDiagonalOnly(hsig, bestArz)
            }
            // Adapt step size sigma - Eq. (5)
            sigma *= FastMath.exp(FastMath.min(1.0, (normps / chiN - 1) * cs / damps))
            val bestFitness = fitness[arindex[0]]
            val worstFitness = fitness[arindex[arindex.size - 1]]
            if (bestValue!! > bestFitness) {
                bestValue = bestFitness
                lastResult = optimum
                optimum = PointValuePair(fitfun.repair(bestArx.getColumn(0)), bestFitness)
            }
            // handle termination criteria
            // Break, if fitness is good enough
            if (stopFitness != 0.0 && bestFitness < stopFitness) converged = true

            val sqrtDiagC = sqrt(diagC).getColumn(0)

            val pcCol = pc!!.getColumn(0)
            for (i in 0 until dimension) {
                if (sigma * FastMath.max(FastMath.abs(pcCol[i]), sqrtDiagC[i]) > stopTolX) break
                if (i >= dimension - 1) converged = true
            }
            for (i in 0 until dimension) {
                if (sigma * sqrtDiagC[i] > stopTolUpX) converged = true

            }
            val historyBest = min(fitnessHistory)
            val historyWorst = max(fitnessHistory)
            if (iterations > 2 &&
                FastMath.max(historyWorst, worstFitness) - FastMath.min(historyBest, bestFitness) < stopTolFun
            ) converged = true

            if (iterations > fitnessHistory.size && historyWorst - historyBest < stopTolHistFun) converged = true

            if (max(diagD) / min(diagD) > 1e7) {
                converged = true
            }
            // user defined termination
            // Adjust step size in case of equal function values (flat fitness)
            if (bestValue == fitness[arindex[(0.1 + lambda / 4.0).toInt()]])
                sigma *= FastMath.exp(0.2 + cs / damps)

            if (iterations > 2 && FastMath.max(historyWorst, bestFitness) - FastMath.min(
                    historyBest,
                    bestFitness
                ) == 0.0
            )
                sigma *= FastMath.exp(0.2 + cs / damps)

            // store best in history
            push(fitnessHistory, bestFitness)
        }

        val rp = mutableListOf<DoubleArray>()
        // generate random offspring
        for (k in 0 until lambda) {
            var arxk: RealMatrix? = null
            arxk = if (diagonalOnly <= 0)
                xmean!!.add(BD!!.multiply(arz.getColumnMatrix(k)).scalarMultiply(sigma)) // m + sig * Normal(0,C)
            else
                xmean!!.add(times(diagD, arz.getColumnMatrix(k)).scalarMultiply(sigma))

            arxk.setColumn(0, arxk.getColumn(0).map {
                when {
                    it > 1.0 -> 1.0
                    it < 0.0 -> 0.0
                    else -> it
                }
            }.toDoubleArray())
            rp.add(arxk!!.getColumn(0))
            copyColumn(arxk, 0, arx, k)
        }
        iterations++
        return rp.toTypedArray()
    }


    // termination criteria
    /** Stop if x-changes larger stopTolUpX.  */
    private var stopTolUpX = 0.0

    /** Stop if x-change smaller stopTolX.  */
    private var stopTolX = 0.0

    /** Stop if fun-changes smaller stopTolFun.  */
    private var stopTolFun = 0.0

    /** Stop if back fun-changes smaller stopTolHistFun.  */
    private var stopTolHistFun = 0.0


    // selection strategy parameters
    /** Number of parents/points for recombination.  */
    private val mu = lambda / 2

    /** log(mu + 0.5), stored for efficiency.  */
    private var logMu2 // NOPMD - using a field here is for performance reasons
            = 0.0

    /** Array for weighted recombination.  */
    private var weights: RealMatrix? = null

    /** Variance-effectiveness of sum w_i x_i.  */
    private var mueff //
            = 0.0
    // dynamic strategy parameters and constants
    /** Overall standard deviation - search volume.  */
    private var sigma = 0.0

    /** Cumulation constant.  */
    private var cc = 0.0

    /** Cumulation constant for step-size.  */
    private var cs = 0.0

    /** Damping for step-size.  */
    private var damps = 0.0

    /** Learning rate for rank-one update.  */
    private var ccov1 = 0.0

    /** Learning rate for rank-mu update'  */
    private var ccovmu = 0.0

    /** Expectation of ||N(0,I)|| == norm(randn(N,1)).  */
    private var chiN = 0.0

    /** Learning rate for rank-one update - diagonalOnly  */
    private var ccov1Sep = 0.0

    /** Learning rate for rank-mu update - diagonalOnly  */
    private var ccovmuSep = 0.0
    // CMA internal values - updated each generation
    /** Objective variables.  */
    private var xmean: RealMatrix? = null

    /** Evolution path.  */
    private var pc: RealMatrix? = null

    /** Evolution path for sigma.  */
    private var ps: RealMatrix? = null

    /** Norm of ps, stored for efficiency.  */
    private var normps = 0.0

    /** Coordinate system.  */
    private var B: RealMatrix? = null

    /** Scaling.  */
    private var D: RealMatrix? = null

    /** B*D, stored for efficiency.  */
    private var BD: RealMatrix? = null

    /** Diagonal of sqrt(D), stored for efficiency.  */
    private var diagD: RealMatrix? = null

    /** Covariance matrix.  */
    private var C: RealMatrix? = null

    /** Diagonal of C, used for diagonalOnly.  */
    private var diagC: RealMatrix? = null

    /** Number of iterations already performed.  */
    public var iterations = 0

    /** History queue of best values.  */
    private var fitnessHistory: DoubleArray = doubleArrayOf()


    /**
     * Initialization of the dynamic search parameters
     * TODO HOLY CRAP!
     * @param guess Initial guess for the arguments of the fitness function.
     */
    private fun initializeCMA(guess: DoubleArray) {
        if (lambda <= 0) {
            throw MathIllegalArgumentException(
                LocalizedCoreFormats.NUMBER_TOO_SMALL_BOUND_EXCLUDED,
                lambda, 0
            )
        }
        // initialize sigma
        val sigmaArray = Array(guess.size) {
            DoubleArray(
                1
            )
        }
        for (i in guess.indices) {
            sigmaArray[i][0] = inputSigma!![i]
        }
        val insigma: RealMatrix = Array2DRowRealMatrix(sigmaArray, false)
        sigma = max(insigma) // overall standard deviation

        // initialize termination criteria
        stopTolUpX = 1e3 * max(insigma)
        stopTolX = 1e-11 * max(insigma)
        stopTolFun = 1e-12
        stopTolHistFun = 1e-13

        // initialize selection strategy parameters
        logMu2 = FastMath.log(mu + 0.5)
        weights = log(sequence(1.0, mu.toDouble(), 1.0)).scalarMultiply(-1.0).scalarAdd(logMu2)
        var sumw = 0.0
        var sumwq = 0.0
        for (i in 0 until mu) {
            val w = (weights as RealMatrix).getEntry(i, 0)
            sumw += w
            sumwq += w * w
        }
        weights = (weights as RealMatrix).scalarMultiply(1 / sumw)
        mueff = sumw * sumw / sumwq // variance-effectiveness of sum w_i x_i

        // initialize dynamic strategy parameters and constants
        cc = (4 + mueff / dimension) /
                (dimension + 4 + 2 * mueff / dimension)
        cs = (mueff + 2) / (dimension + mueff + 3.0)
        damps = (1 + 2 * FastMath.max(
            0.0, FastMath.sqrt(
                (mueff - 1) /
                        (dimension + 1)
            ) - 1
        )) * FastMath.max(
            0.3,
            1 - dimension / (1e-6 + maxIterations)
        ) + cs // minor increment

        ccov1 = 2 / ((dimension + 1.3) * (dimension + 1.3) + mueff)
        ccovmu = FastMath.min(
            1 - ccov1, 2 * (mueff - 2 + 1 / mueff) /
                    ((dimension + 2) * (dimension + 2) + mueff)
        )
        ccov1Sep = FastMath.min(1.0, ccov1 * (dimension + 1.5) / 3)
        ccovmuSep = FastMath.min(1 - ccov1, ccovmu * (dimension + 1.5) / 3)
        chiN = FastMath.sqrt(dimension.toDouble()) *
                (1 - 1 / (4.0 * dimension) + 1 / (21.0 * dimension * dimension))
        // intialize CMA internal values - updated each generation
        xmean = MatrixUtils.createColumnRealMatrix(guess) // objective variables
        diagD = insigma.scalarMultiply(1 / sigma)
        diagC = square(diagD)
        pc = zeros(dimension, 1) // evolution paths for C and sigma
        ps = zeros(dimension, 1) // B defines the coordinate system
        normps = ps!!.frobeniusNorm
        B = eye(dimension, dimension)
        D = ones(dimension, 1) // diagonal D defines the scaling

        BD = times(B, repmat((diagD as RealMatrix).transpose(), dimension, 1))

        C = B!!.multiply(diag(square(D)).multiply(B!!.transpose())) // covariance
        val historySize = 10 + (3 * 10 * dimension / lambda.toDouble()).toInt()
        fitnessHistory = DoubleArray(historySize) // history of fitness values
        for (i in 0 until historySize) {
            fitnessHistory[i] = Double.MAX_VALUE
        }
    }


    /**
     * Update of the evolution paths ps and pc.
     *
     * @param zmean Weighted row matrix of the gaussian random numbers generating
     * the current offspring.
     * @param xold xmean matrix of the previous generation.
     * @return hsig flag indicating a small correction.
     */
    private fun updateEvolutionPaths(zmean: RealMatrix, xold: RealMatrix?): Boolean {
        ps = ps!!.scalarMultiply(1 - cs).add(
            B!!.multiply(zmean).scalarMultiply(
                FastMath.sqrt(cs * (2 - cs) * mueff)
            )
        )
        normps = (ps as RealMatrix).frobeniusNorm
        val hsig = normps /
                FastMath.sqrt(1 - FastMath.pow(1 - cs, 2 * iterations)) /
                chiN < 1.4 + 2 / (dimension.toDouble() + 1)
        pc = pc!!.scalarMultiply(1 - cc)
        if (hsig) {
            pc = (ps as RealMatrix).add(
                xmean!!.subtract(xold).scalarMultiply(FastMath.sqrt(cc * (2 - cc) * mueff) / sigma)
            )
        }
        return hsig
    }

    /**
     * Update of the covariance matrix C for diagonalOnly > 0
     *
     * @param hsig Flag indicating a small correction.
     * @param bestArz Fitness-sorted matrix of the gaussian random values of the
     * current offspring.
     */
    private fun updateCovarianceDiagonalOnly(
        hsig: Boolean,
        bestArz: RealMatrix
    ) {
        // minor correction if hsig==false
        var oldFac: Double = if (hsig) 0.0 else ccov1Sep * cc * (2 - cc)
        oldFac += 1 - ccov1Sep - ccovmuSep
        diagC = diagC!!.scalarMultiply(oldFac) // regard old matrix
            .add(square(pc).scalarMultiply(ccov1Sep)) // plus rank one update
            .add(
                times(diagC, square(bestArz).multiply(weights)) // plus rank mu update
                    .scalarMultiply(ccovmuSep)
            )
        diagD = sqrt(diagC) // replaces eig(C)
        if (diagonalOnly in 2.until(iterations)) {
            // full covariance matrix from now on
            diagonalOnly = 0
            B = eye(dimension, dimension)
            BD = diag(diagD)
            C = diag(diagC)
        }
    }

    /**
     * Update of the covariance matrix C.
     *
     * @param hsig Flag indicating a small correction.
     * @param bestArx Fitness-sorted matrix of the argument vectors producing the
     * current offspring.
     * @param arz Unsorted matrix containing the gaussian random values of the
     * current offspring.
     * @param arindex Indices indicating the fitness-order of the current offspring.
     * @param xold xmean matrix of the previous generation.
     */
    private fun updateCovariance(
        hsig: Boolean, bestArx: RealMatrix,
        arz: RealMatrix, arindex: IntArray,
        xold: RealMatrix?
    ) {
        var negccov = 0.0
        if (ccov1 + ccovmu > 0) {
            val arpos = bestArx.subtract(repmat(xold, 1, mu))
                .scalarMultiply(1 / sigma) // mu difference vectors
            val roneu = pc!!.multiplyTransposed(pc)
                .scalarMultiply(ccov1) // rank one update
            // minor correction if hsig==false
            var oldFac: Double = if (hsig) 0.0 else ccov1 * cc * (2 - cc)
            oldFac += 1 - ccov1 - ccovmu
            if (isActiveCMA) {
                // Adapt covariance matrix C active CMA
                negccov = (1 - ccovmu) * 0.25 * mueff /
                        (FastMath.pow((dimension + 2).toDouble(), 1.5) + 2 * mueff)
                // keep at least 0.66 in all directions, small popsize are most
                // critical
                val negminresidualvariance = 0.66
                // where to make up for the variance loss
                val negalphaold = 0.5
                // prepare vectors, compute negative updating matrix Cneg
                val arReverseIndex = reverse(arindex)
                var arzneg = selectColumns(arz, arReverseIndex.copyOf(mu))
                var arnorms = sqrt(sumRows(square(arzneg)))
                val idxnorms = sortedIndices(arnorms.getRow(0))
                val arnormsSorted = selectColumns(arnorms, idxnorms)
                val idxReverse = reverse(idxnorms)
                val arnormsReverse = selectColumns(arnorms, idxReverse)
                arnorms = divide(arnormsReverse, arnormsSorted)
                val idxInv = inverse(idxnorms)
                val arnormsInv = selectColumns(arnorms, idxInv)
                // check and set learning rate negccov
                val negcovMax = (1 - negminresidualvariance) /
                        square(arnormsInv).multiply(weights).getEntry(0, 0)
                if (negccov > negcovMax) {
                    negccov = negcovMax
                }
                arzneg = times(arzneg, repmat(arnormsInv, dimension, 1))
                val artmp = BD!!.multiply(arzneg)
                val Cneg = artmp.multiply(diag(weights)).multiply(artmp.transpose())
                oldFac += negalphaold * negccov
                C = C!!.scalarMultiply(oldFac)
                    .add(roneu) // regard old matrix
                    .add(
                        arpos.scalarMultiply( // plus rank one update
                            ccovmu + (1 - negalphaold) * negccov
                        ) // plus rank mu update
                            .multiply(
                                times(
                                    repmat(weights, 1, dimension),
                                    arpos.transpose()
                                )
                            )
                    )
                    .subtract(Cneg.scalarMultiply(negccov))
            } else {
                // Adapt covariance matrix C - nonactive
                C = C!!.scalarMultiply(oldFac) // regard old matrix
                    .add(roneu) // plus rank one update
                    .add(
                        arpos.scalarMultiply(ccovmu) // plus rank mu update
                            .multiply(
                                times(
                                    repmat(weights, 1, dimension),
                                    arpos.transpose()
                                )
                            )
                    )
            }
        }
        updateBD(negccov)
    }

    /**
     * Update B and D from C.
     *
     * @param negccov Negative covariance factor.
     */
    private fun updateBD(negccov: Double) {
        if (ccov1 + ccovmu + negccov > 0 &&
            iterations % 1.0 / (ccov1 + ccovmu + negccov) / dimension / 10.0 < 1
        ) {
            // to achieve O(N^2)
            C = triu(C, 0).add(triu(C, 1).transpose())
            // enforce symmetry to prevent complex numbers
            val eig = EigenDecomposition(C)
            B = eig.v // eigen decomposition, B==normalized eigenvectors
            D = eig.d
            diagD = diag(D)
            if (min(diagD) <= 0) {
                for (i in 0 until dimension) {
                    if (diagD!!.getEntry(i, 0) < 0) {
                        diagD!!.setEntry(i, 0, 0.0)
                    }
                }
                val tfac = max(diagD) / 1e14
                C = (C as RealMatrix).add(eye(dimension, dimension).scalarMultiply(tfac))
                diagD = diagD!!.add(ones(dimension, 1).scalarMultiply(tfac))
            }
            if (max(diagD) > 1e14 * min(diagD)) {
                val tfac = max(diagD) / 1e14 - min(diagD)
                C = (C as RealMatrix).add(eye(dimension, dimension).scalarMultiply(tfac))
                diagD = diagD!!.add(ones(dimension, 1).scalarMultiply(tfac))
            }
            diagC = diag(C)
            diagD = sqrt(diagD) // D contains standard deviations now
            BD = times(B, repmat(diagD!!.transpose(), dimension, 1)) // O(n^2)
        }
    }

    /**
     * Sorts fitness values.
     *
     * @param doubles Array of values to be sorted.
     * @return a sorted array of indices pointing into doubles.
     */
    private fun sortedIndices(doubles: DoubleArray): IntArray {
        val dis = arrayOfNulls<DoubleIndex>(doubles.size)
        for (i in doubles.indices) {
            dis[i] = DoubleIndex(doubles[i], i)
        }
        Arrays.sort(dis)
        val indices = IntArray(doubles.size)
        for (i in doubles.indices) {
            indices[i] = dis[i]!!.index
        }
        return indices
    }

    /**
     * Get range of values.
     *
     * @param vpPairs Array of valuePenaltyPairs to get range from.
     * @return a double equal to maximum value minus minimum value.
     */
    private fun valueRange(vpPairs: Array<ValuePenaltyPair?>): Double {
        var max = Double.NEGATIVE_INFINITY
        var min = Double.MAX_VALUE
        for (vpPair in vpPairs) {
            if (vpPair!!.value > max) {
                max = vpPair.value
            }
            if (vpPair.value < min) {
                min = vpPair.value
            }
        }
        return max - min
    }

    /**
     * Used to sort fitness values. Sorting is always in lower value first
     * order.
     */
    private class DoubleIndex(
        /** Value to compare.  */
        private val value: Double,
        /** Index into sorted array.  */
        var index: Int
    ) : Comparable<DoubleIndex?> {
        /** {@inheritDoc}  */
        public override operator fun compareTo(other: DoubleIndex?): Int {
            return value.compareTo(other!!.value)
        }

        /** {@inheritDoc}  */
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            return if (other is DoubleIndex) {
                java.lang.Double.compare(value, other.value) == 0
            } else false
        }

        /** {@inheritDoc}  */
        override fun hashCode(): Int {
            val bits = java.lang.Double.doubleToLongBits(value)
            return 1438542 xor (bits ushr 32).toInt() xor bits.toInt() and -0x1
        }
    }

    /**
     * Stores the value and penalty (for repair of out of bounds point).
     */
    private data class ValuePenaltyPair(val value: Double, val penalty: Double)

    /**
     * Normalizes fitness values to the range [0,1]. Adds a penalty to the
     * fitness value if out of range.
     */
    private inner class FitnessFunction {

        /**
         * Flag indicating whether the objective variables are forced into their
         * bounds if defined
         */
        private val isRepairMode: Boolean = true

        /**
         * @param point Normalized objective variables.
         * @return the objective value + penalty for violated bounds.

        fun value(point: DoubleArray): ValuePenaltyPair {
        val value: Double
        var penalty = 0.0
        if (isRepairMode) {
        val repaired = repair(point)
        value = computeObjectiveValue(repaired)
        penalty = penalty(point, repaired)
        } else {
        value = computeObjectiveValue(point)
        }
        return ValuePenaltyPair(value, penalty)
        }*/

        /**
         * @param x Normalized objective variables.
         * @return `true` if in bounds.
         */
        fun isFeasible(x: DoubleArray): Boolean {
            return x.all { it in 0.0.rangeTo(1.0) }
        }

        /**
         * @param x Normalized objective variables.
         * @return the repaired (i.e. all in bounds) objective variables.
         */
        fun repair(x: DoubleArray): DoubleArray {
            return x.map {
                when {
                    it > 1.0 -> 1.0
                    it < 0.0 -> 0.0
                    else -> it
                }
            }.toDoubleArray()
        }

        /**
         * @param x Normalized objective variables.
         * @param repaired Repaired objective variables.
         * @return Penalty value according to the violation of the bounds.
         */
        private fun penalty(x: DoubleArray, repaired: DoubleArray): Double {
            var penalty = 0.0
            for (i in x.indices) {
                val diff = FastMath.abs(x[i] - repaired[i])
                penalty += diff
            }
            return if (isMinimize) penalty else -penalty
        }

    }

    /**
     * @param size Length of random array.
     * @return an array of Gaussian random numbers.
     */
    private fun randn(size: Int): DoubleArray {
        val randn = DoubleArray(size)
        for (i in 0 until size) {
            randn[i] = random.nextGaussian()
        }
        return randn
    }

    /**
     * @param size Number of rows.
     * @param popSize Population size.
     * @return a 2-dimensional matrix of Gaussian random numbers.
     */
    private fun randn1(size: Int, popSize: Int): RealMatrix {
        val d = Array(size) { DoubleArray(popSize) }
        for (r in 0 until size) {
            for (c in 0 until popSize) {
                d[r][c] = random.nextGaussian()
            }
        }
        return Array2DRowRealMatrix(d, false)
    }
}