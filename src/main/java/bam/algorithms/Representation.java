package bam.algorithms;

/**
 * This interface represents a hypothesis space
 * of dynamics models, along with a specific
 * mapping from intent vectors to rewards.
 *
 * What this doesn't define is the specific
 * transition dynamics, and the set of tasks
 * under these dynamics.  It can therefore be used
 * to configure a learning agent, but doesn't
 * provide a fully specified learning problem.
 *
 * Created by Tyler on 7/26/2017.
 */
public interface Representation {

    /**
     * Returns the number of states in this domain.
     *
     * @return the number of states
     */
    int numStates();

    /**
     * Returns the number of actions available in the given state.
     *
     * @param state the state
     * @return the number of actions available
     */
    int numActions(int state);

    /**
     * Returns the mapping from intent vectors to rewards for this domain.
     *
     * @return the mapping from intents to rewards
     */
    RewardMapping rewards();

    /**
     * Creates a new dynamics model which
     * is suitable for this domain.
     *
     * @return a new, untrained dynamics model
     */
    DynamicsModel newModel();
}
