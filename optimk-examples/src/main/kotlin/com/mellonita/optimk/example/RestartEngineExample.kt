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

package com.mellonita.optimk.example

import com.mellonita.optimk.engine.Engine
import com.mellonita.optimk.engine.Goal
import com.mellonita.optimk.engine.RestartEngine
import com.mellonita.optimk.example.benchmark.Rastrigin
import com.mellonita.optimk.monitor.DefaultMonitor
import com.mellonita.optimk.optimizer.ParticleSwampOptimization
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import kotlin.random.Random


fun main() {
    val d = 50
    val p = 100

    val problem = Rastrigin(d)
    val history = mutableListOf<Double>()

    val engine = RestartEngine(
        problem = problem,
        goal = Goal.Minimize,
        optimizer = ParticleSwampOptimization(d, p, rng = Random(0)),
        threshold = 100,
        //optimizer = BiasedGeneticAlgorithm(d, p, rng = Random(0)),
        monitor = object : DefaultMonitor<DoubleArray>() {
            override fun stop(engine: Engine<DoubleArray>): Boolean {
                history.add(engine.bestFitness)
                if (engine.iterations >= 10_000) {
                    println("Optimization terminated after ${engine.iterations} iterations with best fitness of ${engine.bestFitness}")
                    return true
                }
                return false
            }
        }
    )

    val results = engine.optimize()


    val chart =
        XYChartBuilder()
            .width(1024)
            .height(700)
            .title("Restart Engine Example")
            .xAxisTitle("Iterations")
            .yAxisTitle("Cost")
            .theme(Styler.ChartTheme.GGPlot2)
            .build()

    chart.addSeries("Cost", history.withIndex().filter { it.index.rem(100) == 0 }.map { it.value })

    SwingWrapper(chart).displayChart()

    //history.forEach { println(it) }
}