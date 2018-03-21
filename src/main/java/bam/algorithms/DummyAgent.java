package bam.algorithms;

import java.util.Arrays;

public class DummyAgent implements Agent {

    private double[][] policy;

    private DummyAgent(Representation representation) {
        policy = new double[representation.numStates()][];

        for(int state = 0; state < policy.length; ++state) {
            policy[state] = new double[representation.numActions(state)];
            Arrays.fill(policy[state], 1.0 / policy[state].length);
        }
    }

    public static DummyAgent with(Representation representation) {
        return new DummyAgent(representation);
    }

    @Override
    public void task(String name) { /* Does nothing */ }

    @Override
    public void observe(TeacherAction action) { /* Does nothing */ }

    @Override
    public void observe(TeacherFeedback feedback) { /* Does nothing */ }

    @Override
    public void observe(StateTransition transition) { /* Does nothing */ }

    @Override
    public void integrate() { /* Does nothing */ }

    @Override
    public double[] policy(int state) {
        return policy[state];
    }
}
