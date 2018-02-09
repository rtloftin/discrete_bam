package bam;

import java.util.Random;

/**
 * Created by Tyler on 5/12/2017.
 */
public interface Dynamics {

    /**
     * Gets the size of this bam.domains state space.
     *
     * We repeat these methods a lot, we may want a separate interface
     *
     * @return the number of states
     */
    int numStates();

    /**
     * Gets the number of actions for a state.
     *
     * @param state the current state
     * @return the number of actions available in this state
     */
    int numActions(int state);

    /**
     * Gets the maximum planning depth necessary to
     * perform well in this domain.
     *
     * @return the planning depth
     */
    int depth();

    /**
     * Gets a list of possible successor states
     * given the current state and the action taken.
     *
     * @param state the current state
     * @param action the current action
     * @return the indices of the possible next states
     */
    int[] successors(int state, int action);

    /**
     * Gets the probability of a transition to the specified state,
     * given the current state and action.  This is undefined for
     * states that are not in the successor list, that is, this
     * can return any value if the caller provides an invalid state.
     *
     * @param state the current state
     * @param action the current action
     * @return the probability of transitioning to the possible state
     */
    double[] transitions(int state, int action);

    /**
     * Samples a state from this transition distribution.
     *
     * @param state the current state
     * @param action the action taken
     * @param random a random number generator
     * @return the next state
     */
    default int transition(int state, int action, Random random) {
        int[] next = successors(state, action);
        double[] dist = transitions(state, action);

        double rand = random.nextDouble();
        double total = 0;

        for(int s = 0; s < next.length; ++s) {
            total += dist[s];

            if(total >= rand)
                return next[s];
        }

        return next[0];
    }

    default double simulate(Policy policy,
                            Reward rewards,
                            int start,
                            int steps,
                            Random random) {
        double total = 0.0;
        int state = start;

        for(int step = 0; step < steps; ++step) {
            state = transition(state, policy.action(state, random), random);
            total += rewards.reward(state);
        }

        return total;
    }
}
