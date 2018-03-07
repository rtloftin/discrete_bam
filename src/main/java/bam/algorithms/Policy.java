package bam.algorithms;

import java.util.Random;

/**
 * Represents a method for selecting
 * actions.  A functional interface.
 *
 * Created by Tyler on 10/28/2017.
 */
public interface Policy {

    /**
     * Returns the actions distribution
     * for the specified state.
     *
     * @param state the current state
     * @return the action distribution
     */
    double[] policy(int state);

    /**
     * Samples an action from policy.
     *
     * @param state the current state
     * @param random a random number source
     * @return the sampled action
     */
    default int action(int state, Random random) {
        double[] dist = policy(state);

        double rand = random.nextDouble();
        double sum = 0.0;

        for(int a = 0; a < dist.length; ++a) {
            sum += dist[a];

            if(rand <= sum)
                return a;
        }

        return random.nextInt(dist.length);
    }
}
