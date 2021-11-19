package com.mellonita.optimk.example;

import com.mellonita.optimk.IterationInfo;
import com.mellonita.optimk.Problem;
import com.mellonita.optimk.engine.DefaultEngine;
import com.mellonita.optimk.example.benchmarkfuncs.Rastrigin;
import com.mellonita.optimk.optimizer.BRKGA;
import org.jetbrains.annotations.NotNull;

import static com.mellonita.optimk.EngineKt.GOAL_MIN;

class RastriginOptimization implements Problem<double[]> {

    private final int dimensions;

    public RastriginOptimization(int dimensions) {
        this.dimensions = dimensions;
    }


    @Override
    public double[] decode(@NotNull double[] keys) {
        double[] solution = new double[keys.length];
        for (int i = 0; i < keys.length; i++) {
            solution[i] = (-5.12) + keys[i] * (10.24);
        }
        return solution;
    }

    @Override
    public double objective(double[] candidate) {
        double sum = 0.0;
        for (double x : candidate) {
            sum += Math.pow(x, 2.0) - 20 * Math.cos(x * 2.0 * Math.PI);
        }
        return 20 * dimensions + sum;
    }

    @Override
    public boolean isFeasible(double[] candidate) {
        return true;
    }


    public static void main(String[] args) {
        var rastrigin10D = new Rastrigin(10);
        var optimizer = new BRKGA(10, 1000);

        var engine = new DefaultEngine<>(
                rastrigin10D,
                GOAL_MIN,
                optimizer,
                (iterationInfo) -> iterationInfo.getIteration() > 1000

        );

        IterationInfo<double[]> optimize = engine.optimize();
        System.out.println(optimize);
    }
}

