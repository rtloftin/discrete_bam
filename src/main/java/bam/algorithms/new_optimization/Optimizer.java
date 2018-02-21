package bam.algorithms.new_optimization;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a first order gradient
 * ascent optimization algorithm, and
 * allows us to create parameter objects
 * that will be updated using this
 * algorithm.
 */
public interface Optimizer {

    /**
     * Creates a new parameter vector, which
     * will be updated according to the optimization
     * strategy defined by this object.  Sets the
     * initial values of the parameter vector to
     * the values in the provided array.  The
     * vector will be of the same length as the
     * array of initial values.
     *
     * @param initial the initial values for the vector, also defines the size.
     * @return an initialized parameter vector.
     */
    Parameters parameters(double[] initial);

    /**
     * Gets the name of this optimization strategy.
     *
     * @return the name of this optimization strategy
     */
    String name();

    /**
     * Gets a JSON representation of this optimization strategy.
     *
     * @return a json representation of this optimization strategy
     * @throws JSONException
     */
    default JSONObject serialize() throws JSONException {
        return new JSONObject().put("name", name());
    }
}
