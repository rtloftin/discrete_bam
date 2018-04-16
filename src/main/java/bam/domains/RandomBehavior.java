package bam.domains;

import bam.algorithms.Behavior;

import java.util.Arrays;
import java.util.List;

public class RandomBehavior {

    public static Behavior with(Environment environment, List<? extends Task> tasks) {
        Behavior behavior = Behavior.get();

        double[][] policy = new double[environment.dynamics().numStates()][];

        for(int state = 0; state < policy.length; ++state) {
            policy[state] = new double[environment.dynamics().numActions(state)];
            Arrays.fill(policy[state], 1.0 / policy[state].length);
        }

        for(Task task : tasks)
            behavior.put(task.name(), policy);

        return behavior;
    }
}
