package bam.algorithms.action;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An action model provides a distribution
 * over actions conditioned on a set of
 * action utilities.  It also defines the
 * gradient of the log likelihood of an action
 * with respect to a given set of utilities.
 *
 * Created by Tyler on 8/28/2017.
 */
public interface ActionModel {

    /**
     * Computes the action distribution under
     * this model given the action values.
     *
     * @param values the action values
     * @return the action distribution
     */
    double[] policy(double[] values);

    /**
     * Computes the policy under the this model
     * for the given state-action value function.
     *
     * @param values the action values for each state
     * @return the action distribution for each state.
     */
    default double[][] policy(double[][] values) {
        double[][] p = new double[values.length][];

        for(int state = 0; state < values.length; ++state)
            p[state] = policy(values[state]);

        return p;
    }

    /**
     * Computes the gradient of the log-likelihood of the
     * specified action, given the specified action values,
     * and then multiplies the gradient times the supplied
     * weight and adds it to the array provided.
     *
     * @param action the target action
     * @param values the action values
     * @param gradient the gradient buffer
     * @param weight the wieght of this gradient contribution
     */
    void gradient(int action, double[] values, double[] gradient, double weight);

    /**
     * Computes the gradient of the log-likelihood of the
     * specified action, given the specified action values,
     * and then adds it to the array provided.  This is simply
     * a wrapper for the implementation-defined gradient
     * method with the weight parameter set to 1.0.
     *
     * @param action the target action
     * @param values the action values
     * @param gradient the gradient buffer
     */
    default void gradient(int action, double[] values, double[] gradient) {
        gradient(action, values, gradient, 1.0);
    }

    /**
     * Gets the name of this action model.
     *
     * @return the name of this action model
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
