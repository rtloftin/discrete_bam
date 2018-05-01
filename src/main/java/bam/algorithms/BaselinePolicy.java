package bam.algorithms;

import java.util.Arrays;

public class BaselinePolicy implements Policy {

    private double[][] policy;

    private BaselinePolicy(Dynamics dynamics) {
        policy = new double[dynamics.numStates()][];

        for(int state = 0; state < policy.length; ++state) {
            policy[state] = new double[dynamics.numActions(state)];
            Arrays.fill(policy[state], 1.0 / dynamics.numActions(state));
        }
    }

    public static BaselinePolicy with(Dynamics dynamics){
        return new BaselinePolicy(dynamics);
    }

    @Override
    public double[] policy(int state) {
        return policy[state];
    }
}