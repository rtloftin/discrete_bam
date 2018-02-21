package bam.algorithms.new_optimization;

/**
 * Represents a vector of parameters that may be optimized
 * via gradient ascent. Contains a specific optimization
 * strategy, and stores any variables that may be needed
 * for this strategy, in addition to the parameter vector.
 * Also defines initial values for the vector, but does not
 * specify a regularization term.
 */
public interface Parameters {

    /**
     * Initializes the parameter vector, along with
     * any other variables of the optimization strategy.
     */
    void initialize();

    /**
     * Performs an update on the parameters based
     * on the given gradient sample.
     *
     * @param gradient the gradient, or a stochastic approximation of the gradient.
     */
    void update(double[] gradient);

    /**
     * Gets the current value of the parameter vector.
     *
     * @return an array containing the parameter values.
     */
    double[] value();
}
