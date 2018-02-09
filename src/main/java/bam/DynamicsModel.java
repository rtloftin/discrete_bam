package bam;

import bam.algorithms.optimization.Optimization;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Represents a learned, parametric
 * model of Markov transition dynamics.
 *
 * Created by Tyler on 7/23/2017.
 */
public interface DynamicsModel extends Dynamics {

    /**
     * Initializes the model parameters, and initializes
     * the parameters optimization algorithm.
     *
     * @param optimization the optimization strategy for learning the model
     */
    void initialize(Optimization optimization);

    /**
     * Updates the gradient of the log probability of the specified transition
     * with respect to the model parameters.  Only needs to be correct for
     * subsequent states that are given in the successors list.
     *
     * @param start the current state
     * @param action the current action
     * @param end the next state
     * @param weight the derivative of the likelihood w.r.t. this transition probability
     */
    void train(int start, int action, int end, double weight);

    /**
     * Updates the parameters of the dynamics model.
     */
    void update();

    /**
     * Erases all gradients from the model since the last update.
     */
    void clear();

    /**
     * May render a representation of the learned dynamics.
     *
     * @return an optional image of the dynamics
     */
    default Optional<BufferedImage> render() { return Optional.empty(); }
}
