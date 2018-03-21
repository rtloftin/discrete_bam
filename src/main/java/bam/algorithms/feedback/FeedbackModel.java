package bam.algorithms.feedback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * This interface represents a model
 * of how a teacher provides evaluative
 * feedback.  The model is used to
 * construct critics and interpreters
 *
 * Created by Tyler on 8/27/2017.
 */
public interface FeedbackModel {

    /**
     * Samples from the feedback distribution for
     * the given action based on the set of
     * possible action utilities.
     *
     * @param action the action taken
     * @param values the action utilities
     * @param random a random number source
     * @return the feedback signal generated
     */
    double feedback(int action, double[] values, Random random);

    /**
     * Computes the gradient of the log-likelihood of the given
     * feedback signal in response to the given action, and
     * adds this to the given gradient buffer multiplied by a
     * provided scalar.
     *
     * @param feedback the feedback signal
     * @param action the action taken
     * @param values the action utilities
     * @param gradient the gradient buffer
     * @param weight the weight of this gradient contribution
     */
    void gradient(double feedback, int action, double[] values, double[] gradient, double weight);

    /**
     * Computes the gradient of the log-likelihood of the given
     * feedback signal in response to the given action, and
     * adds this to the given gradient buffer.
     *
     * @param feedback the feedback signal
     * @param action the action taken
     * @param values the action utilities
     * @param gradient the gradient buffer
     */
    default void gradient(double feedback, int action, double[] values, double[] gradient) {
        gradient(feedback, action, values, gradient, 1.0);
    }

    /**
     * Gets the name of this feedback model.
     *
     * @return the name of this feedback model
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
    static FeedbackModel load(JSONObject config) throws JSONException {
        String className = config.getString("class");

        if(className.equals(NoFeedback.class.getSimpleName()))
            return NoFeedback.get();
        else if(className.equals(ASABL.class.getSimpleName()))
            return ASABL.load(config);

        throw new RuntimeException("Unknown Implementation of 'FeedbackModel' requested");
    }
}
