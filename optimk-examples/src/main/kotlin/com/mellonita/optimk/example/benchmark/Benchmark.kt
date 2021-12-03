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

package com.mellonita.optimk.example.benchmark

import com.mellonita.optimk.core.Goal
import com.mellonita.optimk.core.math.valueIn
import com.mellonita.optimk.core.problem.ParallelProblem
import org.jzy3d.chart.AWTChart
import org.jzy3d.chart.Chart
import org.jzy3d.chart.controllers.mouse.camera.AWTCameraMouseController
import org.jzy3d.colors.Color
import org.jzy3d.colors.ColorMapper
import org.jzy3d.colors.colormaps.ColorMapRainbow
import org.jzy3d.maths.Range
import org.jzy3d.plot3d.builder.Builder
import org.jzy3d.plot3d.builder.Mapper
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid
import org.jzy3d.plot3d.primitives.Shape
import org.jzy3d.plot3d.rendering.canvas.Quality
import kotlin.math.abs
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


sealed class Benchmark(final override val d: Int) : ParallelProblem<DoubleArray>, Mapper() {

    override val goal: Goal = Goal.Minimize

    abstract val upperBound: Double
    abstract val lowerBound: Double

    abstract val globalOptima: Double

    override fun invoke(keys: DoubleArray): Double {
        val v = super.invoke(keys)
        return abs(v - globalOptima)
    }

    override fun decode(keys: DoubleArray): DoubleArray {
        return DoubleArray(d) { i ->
            keys[i].valueIn(lowerBound.rangeTo(upperBound))
        }
    }

    override fun f(p0: Double, p1: Double): Double {
        require(d == 2) { "Can only plot 2D functions" }
        return objective(doubleArrayOf(p0, p1))
    }
}


fun KClass<out Benchmark>.plot() {

    val instance = this.primaryConstructor!!.call(2)

    val range = Range(instance.lowerBound.toFloat(), instance.upperBound.toFloat())
    val steps = 100

    val surface: Shape = Builder.buildOrthonormal(OrthonormalGrid(range, steps), instance)
    surface.colorMapper = ColorMapper(ColorMapRainbow(), surface.bounds.zRange)
    surface.faceDisplayed = true
    surface.wireframeDisplayed = true
    surface.wireframeColor = Color.BLACK

    val chart: Chart = AWTChart(Quality.Advanced)
    chart.add(surface)
    chart.addController(AWTCameraMouseController())
    chart.open(this.simpleName, 600, 600)
}
