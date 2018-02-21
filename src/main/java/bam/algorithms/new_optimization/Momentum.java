package bam.algorithms.new_optimization;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Implements gradient ascent with momentum.
 */
public class Momentum implements Optimizer {

    private double learning_rate;
    private double momentum;

    private Momentum(double learning_rate, double momentum) {
        this.learning_rate = learning_rate;
        this.momentum = momentum;
    }

    public static Momentum with(double learning_rate, double momentum) {
        return new Momentum(learning_rate, momentum);
    }

    @Override
    public Parameters parameters(double[] initial) {
        double[] parameters = new double[initial.length];
        double[] delta = new double[initial.length];

        return new Parameters() {

            { initialize(); }

            @Override
            public void initialize() {
                System.arraycopy(initial, 0, parameters, 0, initial.length);
                Arrays.fill(delta, 0.0);
            }

            @Override
            public void update(double[] gradient) {
                for (int i = 0; i < parameters.length; ++i) {
                    delta[i] = (momentum * delta[i]) + (learning_rate * gradient[i]);
                    parameters[i] += delta[i];
                }
            }

            @Override
            public double[] value() {
                return parameters;
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
                .put("learning rate", learning_rate)
                .put("momentum", momentum);
    }
}
