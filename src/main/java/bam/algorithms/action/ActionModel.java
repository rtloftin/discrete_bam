package bam.algorithms.action;

import bam.algorithms.alt.OldNormalizedActionModel;
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
        return new JSONObject()
                .put("name", name())
                .put("class", getClass().getSimpleName());
    }

    /**
     * Loads an implementation of this interface
     * from its JSON representation.
     *
     * @param config the JSON representation of the object
     * @return an instance defined by the JSON representation
     * @throws JSONException
     */
    static ActionModel load(JSONObject config) throws JSONException {
        String className = config.getString("class");

        if(className.equals(RandomActionModel.class.getSimpleName()))
            return RandomActionModel.get();
        else if(className.equals(GreedyActionModel.class.getSimpleName()))
            return GreedyActionModel.load(config);
        else if(className.equals(BoltzmannActionModel.class.getSimpleName()))
            return BoltzmannActionModel.load(config);
        else if(className.equals(NormalizedActionModel.class.getSimpleName()))
            return NormalizedActionModel.load(config);
        else if(className.equals(bam.algorithms.alt.OldNormalizedActionModel.class.getSimpleName()))
            return OldNormalizedActionModel.load(config);

        throw new RuntimeException("Unknown Implementation of 'ActionModel' requested");
    }
}
