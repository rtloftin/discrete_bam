package bam.algorithms.new_variational;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**
 * Represents a variational model of a probability density.
 */
public interface Density {

    /**
     * Represents a single sample from a
     * density, and allows us to compute the
     * gradient with respect to this sample.
     */
    interface Sample {

        /**
         * Gets the vector value of the sample.
         *
         * @return the value of the sample
         */
        double[] value();

        /**
         * Computes the gradient w.r.t parameters of the
         * log-likelihood, based on the likelihood weights
         * provided, and adds it to the given gradient buffer.
         *
         * @param weights the likelihood weights for each output
         * @param gradient the buffer to add the gradient to.
         */
        void gradient(double[] weights, double[] gradient);
    }

    /**
     * Gets the number of parameters needed
     * to represent this density over a vector
     * of the specified size.  The number of
     * dimensions can be inferred from the
     * size of the parameter vector, but there
     * may be more or fewer parameters than
     * there are dimensions.
     *
     * @param dimensions the number of dimensions for the density
     * @return the size of the density's parameter vector.
     */
    int numParameters(int dimensions);

    /**
     * Initializes a parameter vector.
     *
     * @param parameters the parameter vector to be initialized
     */
    void initialize(double[] parameters);

    /**
     * Gets a set of samples from this density for the given
     * parameters.  The number of samples returned will be defined
     * by the density, but not all the samples need be used.  Samples
     * may not be independent though, so it is important to randomize
     * the list if some samples will be ignored.
     *
     * @param parameters the current parameter vector
     * @param random a random number generator
     * @return a list of samples
     */
    List<? extends Sample> sample(double[] parameters, Random random);

    /**
     * Computes the gradient of the regularization term for this
     * density, multiplies it by the given weight term, and adds
     * it to the given gradient buffer.
     *
     * @param parameters the current parameter vector
     * @param gradient the buffer to add the gradient to
     * @param weight the weight of the regularization term in the loss gradient
     */
    void regularize(double[] parameters, double[] gradient, double weight);

    /**
     * Gets the mean of this density for the given parameters.
     *
     * @param parameters the current parameter vector
     * @return the expectation for each dimension of the random vector
     */
    double[] mean(double[] parameters);

    /**
     * Gets the name of this variational model.
     *
     * @return the name of this variational model
     */
    String name();

    /**
     * Gets a JSON representation of this model.
     *
     * @return a json representation of this model
     * @throws JSONException
     */
    default JSONObject serialize() throws JSONException {
        return new JSONObject().put("name", name());
    }
}
