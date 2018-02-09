package bam.algorithms.optimization;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implements the basic gradient ascent optimization strategy.
 *
 * Created by Tyler on 5/2/2017.
 */
public class GradientAscent implements Optimization {

    private double learning_rate;

    private GradientAscent(double learning_rate) {
        this.learning_rate = learning_rate;
    }

    public static GradientAscent with(double learning_rate) {
        return new GradientAscent(learning_rate);
    }

    @Override
    public Optimization.Instance instance(int num_parameters) {
        return (double[] parameters, double[] gradient) -> {
                for(int i=0; i < parameters.length; ++i)
                    parameters[i] += learning_rate * gradient[i];
            };
    }

    @Override
    public String name() {
        return "Gradient Ascent";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("bam.deprecated.learning rate", learning_rate);
    }
}
