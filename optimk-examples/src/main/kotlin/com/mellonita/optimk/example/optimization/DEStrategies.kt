package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.Monitor
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.Engine
import com.mellonita.optimk.engine.Goal
import com.mellonita.optimk.example.benchmark.Sphere
import com.mellonita.optimk.optimizer.DifferentialEvolution
import com.mellonita.optimk.optimizer.MutationStrategy

val problem = Sphere(10) //10-D Sphere function
const val population = 100
const val reportInterval = 1


fun deEngineOf(mutationStrategy: MutationStrategy, monitor: Monitor<DoubleArray>): DefaultEngine<DoubleArray> {
    return DefaultEngine(
        problem = problem,
        goal = Goal.Minimize,
        optimizer = DifferentialEvolution(
            d = problem.d,
            p = population,
            mutation = mutationStrategy
        ),
        monitor = monitor
    )
}

fun monitorOf() = object : Monitor<DoubleArray> {
    override fun stop(engine: Engine<DoubleArray>): Boolean {
        return if (engine.bestFitness < 1E-5) {
            println("Optimization terminated after ${engine.itrCounter} iterations with best fitness of ${engine.bestFitness}")
            true
        } else false
    }
}


fun main() {
    val strategy1 = deEngineOf(DifferentialEvolution.rand1(0.8), monitorOf())
    val strategy2 = deEngineOf(DifferentialEvolution.currentToBest1(0.4, 0.6), monitorOf())
    strategy1.optimize()
    strategy2.optimize()
}