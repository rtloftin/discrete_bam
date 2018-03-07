package bam.algorithms;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Represents a mapping from an intent
 * vector to a reward function.
 *
 * Created by Tyler on 10/9/2017.
 */
public interface RewardMapping {

    /**
     * Gets the reward value for the given state
     * defined by the given intent vector.
     *
     * @param state the state
     * @param intent the intent vector
     * @return
     */
    double reward(int state, double[] intent);

    /**
     * Adds the gradient of the reward at
     * the given state with respect to the
     * intent vector to the given buffer.
     *
     * @param state the state
     * @param intent the intent vector
     * @param weight the backpropagation weight
     * @param gradient the gradient buffer
     */
    void gradient(int state, double[] intent, double weight, double[] gradient);

    /**
     * Returns the number of intent vector dimensions
     * needed by this planning module.  Also resets the
     * task gradient.
     *
     * @return the number of intent dimensions
     */
    int intentSize();

    /**
     * Renders a specific task (defined by an intent vector)
     *
     * @param intent the intent vector
     * @return an optional image of the task
     */
    default Optional<BufferedImage> render(double[] intent) {
        return Optional.empty();
    }
}
