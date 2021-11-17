package com.mellonita.optimk

import com.mellonita.optimk.engine.OptimizationResult

/**
 *
 */
interface Engine<T> {
    /**
     * Perform optimization
     */
    fun optimize(): OptimizationResult<T>
}