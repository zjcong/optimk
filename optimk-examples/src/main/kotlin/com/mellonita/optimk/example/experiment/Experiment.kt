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

package com.mellonita.optimk.example.experiment

import com.mellonita.optimk.Engine
import com.mellonita.optimk.LogLevel
import com.mellonita.optimk.Monitor
import com.mellonita.optimk.monitor.DefaultMonitor

/**
 *
 */
private class ExperimentMonitor<T>(private val itr: Int, private val eval: Int) : DefaultMonitor<T>(LogLevel.INFO) {

    val history: MutableList<Pair<Long, Double>> = mutableListOf()

    //override fun debug(engine: Engine<T>, msg: String) = Unit

    override fun stop(engine: Engine<T>): Boolean {
        history.add(Pair(engine.evaluations, engine.bestFitness))
        return (engine.bestFitness <= 1E-10 || engine.iterations >= itr || engine.evaluations >= eval)
    }
}


/**
 *
 */
class EngineExperiment<T>(
    private val maxIterations: Int,
    private val maxEval: Int,
    names: Set<String>,
    enginesOf: (name: String, monitor: Monitor<T>) -> Engine<T>
) {

    private val engines: Map<String, Engine<T>>
    private var startTime: Long = 0L

    init {
        require(names.isNotEmpty())
        engines = names.associateWith { enginesOf(it, ExperimentMonitor(maxIterations, maxEval)) }
    }

    fun start(): Map<String, MutableList<Pair<Long, Double>>> {
        startTime = System.currentTimeMillis()

        val results = engines.map { (t, u) ->
            u.optimize()
            Pair(t, (u.monitor as ExperimentMonitor).history)
        }.toMap()

        return results
    }
}