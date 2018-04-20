package bam.algorithms.optimization;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ClippedMomentum implements Optimization {

    private final double learning_rate;
    private final double momentum;
    private final double threshold;

    private ClippedMomentum(double learning_rate, double momentum, double threshold) {
        this.learning_rate = learning_rate;
        this.momentum = momentum;
        this.threshold = threshold;
    }

    public static ClippedMomentum with(double learning_rate, double momentum, double threshold) {
        return new ClippedMomentum(learning_rate, momentum, threshold);
    }

    public static ClippedMomentum load(JSONObject config) throws JSONException {
        return with(config.getDouble("learning rate"),
                config.getDouble("momentum"), config.getDouble("threshold"));
    }

    @Override
    public Instance instance(int num_parameters) {
        final double[] delta = new double[num_parameters];
        Arrays.fill(delta, 0.0);

        return (double[] parameters, double[] gradient) -> {
            double norm = 0.0;

            for(int i=0; i < num_parameters; ++i) {
                delta[i] = (momentum * delta[i]) + (learning_rate * gradient[i]);
                norm += delta[i] * delta[i];
            }

            norm = Math.sqrt(norm);

            if(threshold < norm) {
                double scale = threshold / norm;

                for(int i=0; i < num_parameters; ++i)
                    parameters[i] += scale * delta[i];
            } else {
                for(int i=0; i < num_parameters; ++i)
                    parameters[i] += delta[i];
            }
        };
    }

    @Override
    public String name() {
        return "Clipped";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("class", this.getClass().getSimpleName())
                .put("learning rate", learning_rate)
                .put("momentum", momentum)
                .put("threshold", threshold);
    }
}
