package bam.algorithms.feedback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a model of how
 * a teacher estimates the
 * advantage of an action. This
 * will be used inside feedback
 * models that assume feedback depends
 * on advantage.
 *
 * This class is intended for policy
 * independent advantage, rather than
 * policy dependent advantage.
 *
 * Created by Tyler on 8/28/2017.
 */
public interface AdvantageModel {

    /**
     * Gets the advantage of the specified action
     * given the action values provided.
     *
     * @param action the action to compute the advantage of
     * @param values the values of all available actions
     * @return the advantage of the specified action
     */
    double advantage(int action, double[] values);

    /**
     * Computes the gradient of the advantage of the given
     * action with respect to the set of action values, and
     * adds this gradient to the buffer provided, multiplied
     * by the given weight parameter.
     *
     * @param action the action for which we have the advantage
     * @param values the values of all available actions
     * @param gradient the gradient buffer
     * @param weight the weight of this contribution to the gradient
     */
    void gradient(int action, double[] values, double[] gradient, double weight);

    /**
     * Gets the name of this advantage model.
     *
     * @return the name of this advantage model
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
    static AdvantageModel load(JSONObject config) throws JSONException {
        String className = config.getString("class");

        if(className.equals(MaxAdvantage.class.getSimpleName()))
            return MaxAdvantage.get();
        else if(className.equals(MeanAdvantage.class.getSimpleName()))
            return MeanAdvantage.get();
        else if(className.equals(SoftmaxAdvantage.class.getSimpleName()))
            return SoftmaxAdvantage.load(config);

        throw new RuntimeException("Unknown Implementation of 'AdvantageModel' requested");
    }
}
