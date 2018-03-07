package bam.algorithms.new_optimization;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.function.Consumer;

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
     * strategy defined by this object.
     *
     * @param size the number of parameters
     * @param initializer a function that initializes the parameters
     * @return the parameter vector
     */
    Parameters parameters(int size, Consumer<double[]> initializer);

    /**
     * Creates a new parameter vector, which
     * will be updated according to the optimization
     * strategy defined by this object.
     *
     * @param size the number of parameters
     * @param initial an array of initial values for the parameters
     * @return the parameter vector
     */
    default Parameters parameters(int size, double[] initial) {
        return parameters(size, (double[] parameters) -> {
            System.arraycopy(initial, 0, parameters, 0, parameters.length);
        });
    }

    /**
     * Creates a new parameter vector, which
     * will be updated according to the optimization
     * strategy defined by this object.  Parameters
     * will always be initialized to zero.
     *
     * @param size the number of parameters
     * @return the parameter vector
     */
    default Parameters parameters(int size) {
        return parameters(size, (double[] parameters) -> Arrays.fill(parameters, 0.0));
    }

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