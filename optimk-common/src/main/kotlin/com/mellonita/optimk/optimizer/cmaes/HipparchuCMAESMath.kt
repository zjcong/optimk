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

import org.hipparchus.linear.Array2DRowRealMatrix
import org.hipparchus.linear.RealMatrix
import org.hipparchus.util.FastMath
import java.util.*

/**
 * Pushes the current best fitness value in a history queue.
 *
 * @param vals History queue.
 * @param val Current best fitness value.
 */
internal fun push(vals: DoubleArray, `val`: Double) {
    for (i in vals.size - 1 downTo 1) {
        vals[i] = vals[i - 1]
    }
    vals[0] = `val`
}
// -----Matrix utility functions similar to the Matlab build in functions------
/**
 * @param m Input matrix
 * @return Matrix representing the element-wise logarithm of m.
 */
internal fun log(m: RealMatrix): RealMatrix {
    val d = Array(m.rowDimension) {
        DoubleArray(
            m.columnDimension
        )
    }
    for (r in 0 until m.rowDimension) {
        for (c in 0 until m.columnDimension) {
            d[r][c] = FastMath.log(m.getEntry(r, c))
        }
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param m Input matrix.
 * @return Matrix representing the element-wise square root of m.
 */
internal fun sqrt(m: RealMatrix?): RealMatrix {
    val d = Array(m!!.rowDimension) {
        DoubleArray(
            m.columnDimension
        )
    }
    for (r in 0 until m.rowDimension) {
        for (c in 0 until m.columnDimension) {
            d[r][c] = FastMath.sqrt(m.getEntry(r, c))
        }
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param m Input matrix.
 * @return Matrix representing the element-wise square of m.
 */
internal fun square(m: RealMatrix?): RealMatrix {
    val d = Array(m!!.rowDimension) {
        DoubleArray(
            m.columnDimension
        )
    }
    for (r in 0 until m.rowDimension) {
        for (c in 0 until m.columnDimension) {
            val e = m.getEntry(r, c)
            d[r][c] = e * e
        }
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param m Input matrix 1.
 * @param n Input matrix 2.
 * @return the matrix where the elements of m and n are element-wise multiplied.
 */
internal fun times(m: RealMatrix?, n: RealMatrix): RealMatrix {
    val d = Array(m!!.rowDimension) {
        DoubleArray(
            m.columnDimension
        )
    }
    for (r in 0 until m.rowDimension) {
        for (c in 0 until m.columnDimension) {
            d[r][c] = m.getEntry(r, c) * n.getEntry(r, c)
        }
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param m Input matrix 1.
 * @param n Input matrix 2.
 * @return Matrix where the elements of m and n are element-wise divided.
 */
internal fun divide(m: RealMatrix, n: RealMatrix): RealMatrix {
    val d = Array(m.rowDimension) {
        DoubleArray(
            m.columnDimension
        )
    }
    for (r in 0 until m.rowDimension) {
        for (c in 0 until m.columnDimension) {
            d[r][c] = m.getEntry(r, c) / n.getEntry(r, c)
        }
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param m Input matrix.
 * @param cols Columns to select.
 * @return Matrix representing the selected columns.
 */
internal fun selectColumns(m: RealMatrix, cols: IntArray): RealMatrix {
    val d = Array(m.rowDimension) { DoubleArray(cols.size) }
    for (r in 0 until m.rowDimension) {
        for (c in cols.indices) {
            d[r][c] = m.getEntry(r, cols[c])
        }
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param m Input matrix.
 * @param k Diagonal position.
 * @return Upper triangular part of matrix.
 */
internal fun triu(m: RealMatrix?, k: Int): RealMatrix {
    val d = Array(m!!.rowDimension) {
        DoubleArray(
            m.columnDimension
        )
    }
    for (r in 0 until m.rowDimension) {
        for (c in 0 until m.columnDimension) {
            d[r][c] = if (r <= c - k) m.getEntry(r, c) else 0.0
        }
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param m Input matrix.
 * @return Row matrix representing the sums of the rows.
 */
internal fun sumRows(m: RealMatrix): RealMatrix {
    val d = Array(1) { DoubleArray(m.columnDimension) }
    for (c in 0 until m.columnDimension) {
        var sum = 0.0
        for (r in 0 until m.rowDimension) {
            sum += m.getEntry(r, c)
        }
        d[0][c] = sum
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param m Input matrix.
 * @return the diagonal n-by-n matrix if m is a column matrix or the column
 * matrix representing the diagonal if m is a n-by-n matrix.
 */
internal fun diag(m: RealMatrix?): RealMatrix {
    return if (m!!.columnDimension == 1) {
        val d = Array(m.rowDimension) {
            DoubleArray(
                m.rowDimension
            )
        }
        for (i in 0 until m.rowDimension) {
            d[i][i] = m.getEntry(i, 0)
        }
        Array2DRowRealMatrix(d, false)
    } else {
        val d = Array(m.rowDimension) {
            DoubleArray(
                1
            )
        }
        for (i in 0 until m.columnDimension) {
            d[i][0] = m.getEntry(i, i)
        }
        Array2DRowRealMatrix(d, false)
    }
}

/**
 * Copies a column from m1 to m2.
 *
 * @param m1 Source matrix.
 * @param col1 Source column.
 * @param m2 Target matrix.
 * @param col2 Target column.
 */
internal fun copyColumn(
    m1: RealMatrix?, @Suppress("SameParameterValue") col1: Int,
    m2: RealMatrix, col2: Int
) {
    for (i in 0 until m1!!.rowDimension) {
        m2.setEntry(i, col2, m1.getEntry(i, col1))
    }
}

/**
 * @param n Number of rows.
 * @param m Number of columns.
 * @return n-by-m matrix filled with 1.
 */
@Suppress("SameParameterValue")
internal fun ones(n: Int, m: Int): RealMatrix {
    val d = Array(n) { DoubleArray(m) }
    for (r in 0 until n) {
        Arrays.fill(d[r], 1.0)
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param n Number of rows.
 * @param m Number of columns.
 * @return n-by-m matrix of 0 values out of diagonal, and 1 values on
 * the diagonal.
 */
internal fun eye(n: Int, m: Int): RealMatrix {
    val d = Array(n) { DoubleArray(m) }
    for (r in 0 until n) {
        if (r < m) {
            d[r][r] = 1.0
        }
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param n Number of rows.
 * @param m Number of columns.
 * @return n-by-m matrix of zero values.
 */
internal fun zeros(n: Int, m: Int): RealMatrix {
    return Array2DRowRealMatrix(n, m)
}

/**
 * @param mat Input matrix.
 * @param n Number of row replicates.
 * @param m Number of column replicates.
 * @return a matrix which replicates the input matrix in both directions.
 */
internal fun repmat(mat: RealMatrix?, n: Int, m: Int): RealMatrix {
    val rd = mat!!.rowDimension
    val cd = mat.columnDimension
    val d = Array(n * rd) { DoubleArray(m * cd) }
    for (r in 0 until n * rd) {
        for (c in 0 until m * cd) {
            d[r][c] = mat.getEntry(r % rd, c % cd)
        }
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param start Start value.
 * @param end End value.
 * @param step Step size.
 * @return a sequence as column matrix.
 */
@Suppress("SameParameterValue")
internal fun sequence(start: Double, end: Double, step: Double): RealMatrix {
    val size = ((end - start) / step + 1).toInt()
    val d = Array(size) { DoubleArray(1) }
    var value = start
    for (r in 0 until size) {
        d[r][0] = value
        value += step
    }
    return Array2DRowRealMatrix(d, false)
}

/**
 * @param m Input matrix.
 * @return the maximum of the matrix element values.
 */
internal fun max(m: RealMatrix?): Double {
    var max = -Double.MAX_VALUE
    for (r in 0 until m!!.rowDimension) {
        for (c in 0 until m.columnDimension) {
            val e = m.getEntry(r, c)
            if (max < e) {
                max = e
            }
        }
    }
    return max
}

/**
 * @param m Input matrix.
 * @return the minimum of the matrix element values.
 */
internal fun min(m: RealMatrix?): Double {
    var min = Double.MAX_VALUE
    for (r in 0 until m!!.rowDimension) {
        for (c in 0 until m.columnDimension) {
            val e = m.getEntry(r, c)
            if (min > e) {
                min = e
            }
        }
    }
    return min
}

/**
 * @param m Input array.
 * @return the maximum of the array values.
 */
internal fun max(m: DoubleArray): Double {
    var max = -Double.MAX_VALUE
    for (r in m.indices) {
        if (max < m[r]) {
            max = m[r]
        }
    }
    return max
}

/**
 * @param m Input array.
 * @return the minimum of the array values.
 */
internal fun min(m: DoubleArray): Double {
    var min = Double.MAX_VALUE
    for (r in m.indices) {
        if (min > m[r]) {
            min = m[r]
        }
    }
    return min
}

/**
 * @param indices Input index array.
 * @return the inverse of the mapping defined by indices.
 */
internal fun inverse(indices: IntArray): IntArray {
    val inverse = IntArray(indices.size)
    for (i in indices.indices) {
        inverse[indices[i]] = i
    }
    return inverse
}

/**
 * @param indices Input index array.
 * @return the indices in inverse order (last is first).
 */
internal fun reverse(indices: IntArray): IntArray {
    val reverse = IntArray(indices.size)
    for (i in indices.indices) {
        reverse[i] = indices[indices.size - i - 1]
    }
    return reverse
}