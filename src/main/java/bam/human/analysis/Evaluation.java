package bam.human.analysis;

import bam.algorithms.Behavior;
import bam.algorithms.Policy;
import bam.domains.Environment;
import bam.domains.Task;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Repreesents a specific method of evaluating a
 * multi-task behavior.  This will generally be
 * attached to a specific environment and a set of
 * named tasks.
 *
 * @param <T> the type of the object used to record the results of the evaluation
 */
public interface Evaluation<T> {

    static Evaluation<Double> mean(Environment environment, int episodes) {
        double[][] pi = new double[environment.dynamics().numStates()][];

        for(int state = 0; state < pi.length; ++state) {
            pi[state] = new double[environment.dynamics().numActions(state)];
            Arrays.fill(pi[state], 1.0 / pi[state].length);
        }

        Policy baseline = (int state) -> pi[state];

        return (Behavior behavior) -> {
            double total = 0.0;

            int depth = environment.dynamics().depth();
            Random random = ThreadLocalRandom.current();

            for(Task task : environment.tasks()) {
                Policy policy = behavior.policy(task.name());

                if(null == policy)
                    policy = baseline;

                for (int episode = 0; episode < episodes; ++episode) {
                    int start = task.initial(random);

                    total += environment.dynamics().simulate(policy, task, start, depth, random) / depth;
                }
            }

            return total / episodes;
        };
    }

    T of(Behavior behavior);
}
