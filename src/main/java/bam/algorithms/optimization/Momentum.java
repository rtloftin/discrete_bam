package bam.algorithms.optimization;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Implements gradient ascent with momentum.
 *
 * Created by Tyler on 5/2/2017.
 */
public class Momentum implements Optimization {

    private double learning_rate;
    private double momentum;

    private Momentum(double learning_rate, double momentum) {
        this.learning_rate = learning_rate;
        this.momentum = momentum;
    }

    public static Momentum with(double learning_rate, double momentum) {
        return new Momentum(learning_rate, momentum);
    }

    public static Momentum load(JSONObject config) throws JSONException {
        return with(config.getDouble("learning rate"), config.getDouble("momentum"));
    }

    @Override
    public Optimization.Instance instance(int num_parameters) {
        final double[] delta = new double[num_parameters];
        Arrays.fill(delta, 0.0);

        return (double[] parameters, double[] gradient) -> {
                for (int i = 0; i < parameters.length; ++i) {
                    delta[i] = (momentum * delta[i]) + (learning_rate * gradient[i]);
                    parameters[i] += delta[i];
                }
            };
    }

    @Override
    public String name() {
        return "Momentum";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("class", getClass().getSimpleName())
                .put("learning rate", learning_rate)
                .put("momentum", momentum);
    }
}
