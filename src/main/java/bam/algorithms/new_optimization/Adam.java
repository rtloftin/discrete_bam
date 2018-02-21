package bam.algorithms.new_optimization;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Implements the Adam update strategy.
 */
public class Adam implements Optimizer {

    private double learning_rate;
    private double mean_decay;
    private double variance_decay;
    private double offset;

    private Adam(double learning_rate, double mean_decay, double variance_decay,  double offset) {
        this.learning_rate = learning_rate;
        this.mean_decay = mean_decay;
        this.variance_decay = variance_decay;
        this.offset = offset;
    }

    public static Adam with(double learning_rate, double mean_decay, double variance_decay, double offset) {
        return new Adam(learning_rate, mean_decay, variance_decay, offset);
    }

    @Override
    public Parameters parameters(double[] initial) {
        double[] parameters = new double[initial.length];
        double[] first_moment = new double[initial.length];
        double[] second_moment = new double[initial.length];

        return new Parameters() {

            { initialize(); }

            @Override
            public void initialize() {
                System.arraycopy(initial, 0, parameters, 0, initial.length);
                Arrays.fill(first_moment, 0.0);
                Arrays.fill(second_moment, 0.0);
            }

            @Override
            public void update(double[] gradient) {
                for(int i=0; i < parameters.length; ++i) {
                    first_moment[i] = mean_decay * first_moment[i] + (1.0 - mean_decay) * gradient[i];
                    second_moment[i] = variance_decay * second_moment[i]
                            + (1.0 - variance_decay) * gradient[i] * gradient[i];
                    parameters[i] += learning_rate * first_moment[i] / (Math.sqrt(second_moment[i]) + offset);
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
        return "Adam";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("learning rate", learning_rate)
                .put("mean decay", mean_decay)
                .put("variance decay", variance_decay)
                .put("offset", offset);
    }
}
