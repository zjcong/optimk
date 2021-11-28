package com.mellonita.optimk.example.benchmark

import com.mellonita.optimk.Goal
import com.mellonita.optimk.Problem
import com.mellonita.optimk.math.valueIn
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


sealed class Benchmark(final override val dimensions: Int) : Problem<DoubleArray>, Mapper() {

    override val goal: Goal = Goal.Minimize

    abstract val upperBound: Double
    abstract val lowerBound: Double

    abstract val globalOptima: Double

    override fun invoke(keys: DoubleArray): Double {
        val v = super.invoke(keys)
        return abs(v - globalOptima)
    }

    override fun decode(keys: DoubleArray): DoubleArray {
        return DoubleArray(dimensions) { i ->
            keys[i].valueIn(lowerBound.rangeTo(upperBound))
        }
    }

    override fun f(p0: Double, p1: Double): Double {
        require(dimensions == 2) { "Can only plot 2D functions" }
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
