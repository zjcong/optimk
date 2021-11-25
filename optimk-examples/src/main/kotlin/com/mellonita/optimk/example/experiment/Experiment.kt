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

import com.mellonita.optimk.engine.Engine
import com.mellonita.optimk.monitor.DefaultMonitor
import com.mellonita.optimk.monitor.Monitor

/**
 *
 */
private class ExperimentMonitor<T>(private val itr: Long) : DefaultMonitor<T>() {

    val history: MutableList<Double> = mutableListOf()

    //override fun debug(engine: Engine<T>, msg: String) = Unit

    override fun stop(engine: Engine<T>): Boolean {
        history.add(engine.bestFitness)
        return engine.iterations >= itr
    }
}


/**
 *
 */
class Experiment<T>(
    private val maxIterations: Long,
    names: Set<String>,
    enginesOf: (name: String, monitor: Monitor<T>) -> Engine<T>
) {

    private val engines: Map<String, Engine<T>>
    private var startTime: Long = 0L

    init {
        require(names.isNotEmpty())
        engines = names.associateWith { enginesOf(it, ExperimentMonitor(maxIterations)) }
    }

    fun start(): Map<String, MutableList<Double>> {
        startTime = System.currentTimeMillis()

        val results = engines.map { (t, u) ->
            u.optimize()
            Pair(t, (u.monitor as ExperimentMonitor).history)
        }.toMap()

        return results
    }
}