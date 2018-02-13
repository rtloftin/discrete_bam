package bam.algorithms;

import bam.Dynamics;
import bam.Policy;

import java.util.Arrays;

public class Baseline implements Policy {

    private double[][] policy;

    private Baseline(Dynamics dynamics) {
        policy = new double[dynamics.numStates()][];

        for(int state = 0; state < policy.length; ++state) {
            policy[state] = new double[dynamics.numActions(state)];
            Arrays.fill(policy[state], 1.0 / dynamics.numActions(state));
        }
    }

    public static Baseline with(Dynamics dynamics){
        return new Baseline(dynamics);
    }

    @Override
    public double[] policy(int state) {
        return policy[state];
    }
}