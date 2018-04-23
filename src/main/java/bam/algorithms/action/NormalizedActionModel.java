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

        for(int action = 0; action < values.length; ++action) {
            double delta = mean - values[action];
            variance += delta * delta;
        }

        double deviation = (0.0 == variance) ? 1.0 : Math.sqrt(variance / values.length);

        // Compute policy
        double partition = 0.0;
        double[] policy = new double[values.length];

        for(int action = 0; action < values.length; ++action) {
            policy[action] = Math.exp(beta * (values[action] - values[0]) / deviation);
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

        for(int a = 0; a < values.length; ++a)
            mean += values[a];

        mean /= values.length;

        // Compute standard deviation
        double variance = 0.0;

        for(int a = 0; a < values.length; ++a) {
            double delta = mean - values[a];
            variance += delta * delta;
        }

        variance = (0.0 == variance) ? 1.0 : (variance / values.length);
        double deviation = Math.sqrt(variance);

        // Compute policy
        double partition = 0.0;
        double[] policy = new double[values.length];

        for(int a = 0; a < values.length; ++a) {
            policy[a] = Math.exp(beta * (values[a] - values[0]) / deviation);
            partition += policy[a];
        }

        if(!Double.isFinite(partition)) {
            System.err.println("Numerical error encountered");
            System.exit(1);
        }

        for(int a = 0; a < values.length; ++a)
            policy[a] /= partition;

        // compute gradients
        for(int a = 0; a < values.length; ++a) {
            double sigma = (values[a] - mean) / (values.length * deviation);
            double derivative;

            if(action == a) {
                derivative = beta * (deviation - (values[action] * sigma) ) / variance;
            } else {
                derivative = -beta * values[action] * sigma / variance;
            }

            for(int b = 0; b < values.length; ++b) {
                if(a == b) {
                    derivative -= beta * policy[b] * (deviation - (values[b] * sigma) ) / variance;
                } else {
                    derivative += beta * policy[b] * values[b] * sigma / variance;
                }
            }

            gradient[a] += weight * derivative;
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
