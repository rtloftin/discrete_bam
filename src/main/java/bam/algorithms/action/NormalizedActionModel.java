package bam.algorithms.action;

import org.json.JSONException;
import org.json.JSONObject;

public class NormalizedActionModel implements ActionModel {

    private final double beta;

    private NormalizedActionModel(double beta) {
        this.beta = beta;
    }

    public static NormalizedActionModel get() {
        return new NormalizedActionModel(1.0);
    }

    public static NormalizedActionModel beta(double beta) {
        return new NormalizedActionModel(beta);
    }

    public static NormalizedActionModel load(JSONObject config) throws JSONException {
        return beta(config.getDouble("beta"));
    }

    @Override
    public double[] policy(double[] values) {

        // Compute mean
        double mean = 0.0;

        for(int action = 0; action < values.length; ++action)
            mean += values[action];

        mean /= values.length;

        // Compute standard deviation
        double variance = 0.0;

        for(int action = 0; action < values.length; ++action)
            variance += (mean - values[action]) * (mean - values[action]);

        double deviation = (0.0 == variance) ? 1.0 : Math.sqrt(variance / values.length);

        // Compute policy
        double partition = 0.0;
        double[] policy = new double[values.length];

        for(int action = 0; action < values.length; ++action) {
            policy[action] = Math.exp(beta * values[action] / deviation);
            partition += policy[action];
        }

        for(int action = 0; action < values.length; ++action)
            policy[action] /= partition;

        // Return the policy
        return policy;
    }

    @Override
    public void gradient(int action, double[] values, double[] gradient, double weight) {

        // Compute mean
        double mean = 0.0;

        for(int i=0; i < values.length; ++i)
            mean += values[i];

        mean /= values.length;

        // Compute standard deviation
        double variance = 0.0;

        for(int i=0; i < values.length; ++i)
            variance += (mean - values[i]) * (mean - values[i]);

        double deviation = (0.0 == variance) ? 1.0 : Math.sqrt(variance / values.length);

        // Compute policy
        double partition = 0.0;
        double[] policy = new double[values.length];

        for(int i=0; i < values.length; ++i) {
            policy[i] = Math.exp(beta * values[i] / deviation);
            partition += policy[i];
        }

        for(int i=0; i < values.length; ++i)
            policy[i] /= partition;

        // compute gradients
        for(int i=0; i < values.length; ++i) {
            double sigma = (values[i] - mean) / (deviation * values.length);
            double delta;

            if(action == i) {
                delta = beta * (1.0 - policy[action]) * (deviation - values[action] * sigma) / (deviation * deviation);

                for(int j=0; j < values.length; ++j)
                    if(j != action)
                        delta -= beta * policy[j] * values[j] * sigma / (deviation * deviation);
            } else {
                delta = beta * (1.0 - policy[action]) * values[action] * sigma / (deviation * deviation);
                delta -= beta * policy[i] * (deviation - values[i] * sigma) / (deviation * deviation);

                for(int j=0; j < values.length; ++j)
                    if(j != action && j != i)
                        delta -= beta * policy[j] * values[j] * sigma / (deviation * deviation);
            }

            gradient[i] += weight * delta;
        }
    }

    @Override
    public String name() {
        return "Normalized Boltzmann";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("class", getClass().getSimpleName())
                .put("beta", beta);
    }
}
