package bam.algorithms.planning;

/**
 * This interface represents a differentiable,
 * representation of the transition dynamics of a
 * discrete domain.  It allows for backpropagation
 * against the transition probabilities.
 *
 * Created by Tyler on 5/9/2017.
 */
public interface PlanningGraph {

    ////////////////////////
    // Planning Structure //
    ////////////////////////

    /**
     * Returns the appropriate stochastic_planning depth for
     * doing value iteration on this model.
     *
     * @return the correct stochastic_planning depth
     */
    int depth();

    /**
     * Returns the total number of planning nodes
     * at the given planning depth
     *
     * @param depth the planning depth
     * @return the number of nodes
     */
    int numNodes(int depth);

    /**
     * Returns the number of options
     * available in the given node.
     *
     * @param depth the planning depth
     * @param node the node
     * @return the number of actions
     */
    int numOptions(int depth, int node);

    /////////////////////////
    // Transition Dynamics //
    /////////////////////////

    /**
     * Gets a list of possible successor nodes
     * given the current node and option taken.
     *
     * @param depth the planning depth
     * @param node the current node
     * @param option the current option
     * @return the indices of the possible successor nodes
     */
    int[] successors(int depth, int node, int option);

    /**
     * Gets the transition distribution given
     * the current node and option.
     *
     * @param depth the planning depth
     * @param node the current node
     * @param option the current option
     * @return the transition probability distribution
     */
    double[] transitions(int depth, int node, int option);

    /**
     * Updates the gradient of the log probability of the specified transition
     * with respect to the model parameters.  Only needs to be correct for
     * subsequent nodes that are given in the successors list.
     *
     * @param depth the planning depth
     * @param start the current node
     * @param action the current option
     * @param end the next node
     * @param weight the derivative of the likelihood w.r.t. this transition probability
     */
    void train(int depth, int start, int action, int end, double weight);


    /////////////////////
    // Reward Function //
    /////////////////////

    /**
     * Returns the reward value at a specified depth and node
     *
     * @param depth the stochastic_planning depth
     * @param node the current node
     * @return the reward value
     */
    double reward(int depth, int node);

    /**
     * Updates the gradient of the reward function for
     * the specified planning depth and node.
     *
     * @param depth the planning depth
     * @param node the current node
     * @param weight the derivative of the likelihood w.r.t. this reward signal
     */
    void train(int depth, int node, double weight);
}
