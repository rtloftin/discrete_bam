package bam.algorithms;

import bam.*;
import bam.algorithms.planning.MaxPlanner;
import bam.algorithms.action.GreedyActionModel;

/**
 * This class simply computes and stores an
 * optimal policy and the associated
 * Q-function.  This class is used internally
 * by experimental classes to generate baselines
 * against which results can be compared.
 *
 * Created by Tyler on 9/6/2017.
 */
public class Expert implements Policy {

    private double[][] Q;
    private double[][] PI;

    private Expert(Dynamics dynamics, Reward rewards) {

        // Get optimal Q-function
        Q = MaxPlanner.algorithm().planner(dynamics, rewards).values();

        // Get optimal policy
        PI = GreedyActionModel.get().policy(Q);
    }

    public static Expert with(Dynamics dynamics, Reward rewards) {
        return new Expert(dynamics, rewards);
    }

    public double[] values(int state) {
        return Q[state];
    }

    @Override
    public double[] policy(int state) { return PI[state]; }
}
