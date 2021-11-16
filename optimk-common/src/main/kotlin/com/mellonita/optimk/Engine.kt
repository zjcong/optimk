package com.mellonita.optimk

/**
 *
 */
interface Engine<T> {
    /**
     * Perform optimization
     */
    fun optimize(): OptimizationResult<T>
}