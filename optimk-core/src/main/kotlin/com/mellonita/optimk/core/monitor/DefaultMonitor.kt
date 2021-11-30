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

package com.mellonita.optimk.core.monitor

import com.mellonita.optimk.core.LogLevel
import com.mellonita.optimk.core.Monitor


/**
 * This class is the default monitor
 */
public abstract class DefaultMonitor<T>(private val level: LogLevel) : Monitor<T> {

    override fun log(level: LogLevel, engine: com.mellonita.optimk.core.Engine<T>, msg: String) {
        if (level < this.level) {
            return
        }
        println("${level.name} [${engine.name}]:  $msg")
    }
}