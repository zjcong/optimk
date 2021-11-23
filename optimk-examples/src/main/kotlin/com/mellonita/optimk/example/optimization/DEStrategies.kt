package com.mellonita.optimk.example.optimization

import com.mellonita.optimk.Monitor
import com.mellonita.optimk.engine.DefaultEngine
import com.mellonita.optimk.engine.Engine
import com.mellonita.optimk.engine.Goal
import com.mellonita.optimk.example.benchmark.Sphere
import com.mellonita.optimk.optimizer.DifferentialEvolution
import com.mellonita.optimk.optimizer.MutationStrategy
import java.io.File

val problem = Sphere(50) //10-D Sphere function
const val population = 100

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

        //Save state every 100 iterations
        if (engine.iterations.rem(100L) == 0L)
            engine.suspendTo(File("suspended_engine.bin"))

        if (engine.bestFitness >= 1E-5) return false
        println("Optimization terminated after ${engine.iterations} iterations with best fitness of ${engine.bestFitness}")
        return true
    }
}


fun main() {

    val engine = deEngineOf(
        mutationStrategy = DifferentialEvolution.rand1(0.8),
        monitor = monitorOf()
    )

    val result1 = engine.optimize()

    // resume from the saved state
    val resumedEngine = Engine.resumeFrom<DefaultEngine<DoubleArray>>(File("suspended_engine.bin"))

    val result2 = resumedEngine.optimize()

    assert(result1.contentEquals(result2))
}