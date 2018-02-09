package bam.algorithms.planning;

/**
 * Represents a specific instance of a planning
 * algorithm, associated with a specific planning
 * network, and a set of allocated buffers.
 *
 * Not sure what the planning module is for then, it seems
 * that maybe the planning module itself is deprecated, the logic
 * encoded in that can simply be captured by the planning network
 * and the
 */
public interface Planner {

    /**
     * Gets the number of output states, that is, the
     * number of states at the top layer of the transition
     * model.  If layers are not defined, this will
     * be equal to the number of states in the model.
     * This will always be equal to the first dimension
     * of the Q-function array.
     *
     * @return the number of output states
     */
    int numStates();

    /**
     * Gets the number of actions available
     * in the given output state.
     *
     * @return the number of available actions
     */
    int numActions(int state);

    /**
     * Computes and returns the state-action value function. Also
     * updates internal data structures necessary for training.
     *
     * @return the state-action value function
     */
    double[][] values();

    /**
     * Backpropagates the Jacobian of the objective w.r.t the
     * state-action value function through the stochastic_planning process.
     * This must be called after the actionValues() method
     * has been called.
     *
     * @param jacobian the jacobian w.r.t. the value function
     */
    void train(double[][] jacobian);
}
