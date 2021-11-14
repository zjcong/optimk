import com.mellonita.optimk.bga.BGAOptimizer
import com.mellonita.optimk.common.Problem
import com.mellonita.optimk.common.DefaultEngine
import com.mellonita.optimk.common.StoppingCriterion
import io.kotest.core.spec.style.StringSpec
import kotlin.math.PI
import kotlin.math.cos

class BGATests : StringSpec({
    "Basic tests"{

        val rastrigin10D: Problem<DoubleArray> = object : Problem<DoubleArray>() {
            override val dimensions: Int
                get() = 10

            override fun decoder(randomKeys: DoubleArray): DoubleArray {
                return randomKeys.map { it * 10.24 - 5.12 }.toDoubleArray()
            }

            override fun feasible(candidate: DoubleArray): Boolean = true

            override fun fitness(candidate: DoubleArray): Double {
                return 10 * dimensions + candidate.sumOf {
                    it * it - 10 * cos(2 * PI * it)
                }
            }
        }

        val engine = DefaultEngine(
            problem = rastrigin10D,
            optimizerClass = BGAOptimizer::class,
            params = mapOf(
                "population" to 100,
                "elites" to 0.25,
                "mutants" to 0.30,
                "bias" to 0.8
            ),
            stoppingCriterion = StoppingCriterion(
                whenFitnessReach = 1E-6,
                afterMilliseconds = 200
            )
        )

        val result = engine.optimize()

        println(result)
    }
})