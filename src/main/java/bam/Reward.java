package bam;

public interface Reward {

    /**
     * Gets the task reward for the give state.
     *
     * @param state the current state
     * @return the reward value for that state
     */
    double reward(int state);

}
