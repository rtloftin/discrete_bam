package bam.algorithms.variational;

import bam.algorithms.optimization.Optimization;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;

/**
 * A distribution with a Gaussian
 * prior, but represented by a single
 * point at its mean.
 *
 * Created by Tyler on 7/23/2017.
 */
public class PointDensity implements Variational {

    public static class Builder {

        // Prior distribution
        private double prior_mean  = 0.0;
        private double prior_deviation = 1.0;

        // Optimization strategy
        private Optimization optimization = null;

        private Builder() {}

        public Builder priorMean(double prior_mean) {
            this.prior_mean = prior_mean;

            return this;
        }

        public Builder priorDeviation(double prior_deviation) {
            this.prior_deviation = prior_deviation;

            return this;
        }

        public Builder optimization(Optimization optimization) {
            this.optimization = optimization;

            return this;
        }

        public PointDensity build() {
            if(null == optimization)
                throw new RuntimeException("DUMBASS!!! - No optimization strategy defined");

            return new PointDensity(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private Builder config;

    private PointDensity(Builder builder) { this.config = config; }

    private class Density implements Variational.Density {

        private int dimensions;

        private Optimization.Instance optimizer;

        private double[] parameters;
        private double[] gradient;

        private Density(int dimensions) {
            this.dimensions = dimensions;

            parameters = new double[dimensions];
            gradient = new double[dimensions];
        }

        @Override
        public int size() { return dimensions; }

        @Override
        public int numSamples() { return 1; }

        @Override
        public void nextSample() { /* Does nothing */ }

        @Override
        public double[] value() { return parameters; }

        @Override
        public double[] mean() {
            double[] mean = new double[dimensions];
            System.arraycopy(parameters, 0, mean, 0, dimensions);

            return mean;
        }

        @Override
        public void train(double[] jacobian) {
            for(int i=0; i < dimensions; ++i)
                gradient[i] += jacobian[i];
        }

        @Override
        public void initialize() {

            // Initialize parameters and gradient
            Arrays.fill(parameters, config.prior_mean);
            Arrays.fill(gradient, 0.0);

            // Initialize optimization strategy
            optimizer = config.optimization.instance(dimensions);
        }

        @Override
        public void update() {

            // Compute regularization term
            for(int i=0; i < dimensions; ++i)
                gradient[i] += (config.prior_mean - parameters[i]) / (config.prior_deviation * config.prior_deviation);

            // Update parameters
            optimizer.update(parameters, gradient);

            // Reset gradient
            Arrays.fill(gradient, 0.0);
        }

        @Override
        public void clear() { Arrays.fill(gradient, 0.0); }
    }

    @Override
    public Density density(final int dimensions, Random random) {
        return this.new Density(dimensions);
    }

    @Override
    public String name() {
        return "Point Estimate";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("prior mean", config.prior_mean)
                .put("prior deviation", config.prior_deviation);
    }
}
