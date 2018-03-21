package bam.algorithms.variational;

import bam.algorithms.optimization.Optimization;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;

/**
 * A variational model of a gaussian density.
 *
 * Created by Tyler on 5/9/2017.
 */
public class GaussianDensity implements Variational {

    public static class Builder {

        // Sampling method
        private int num_samples = 1;
        private double resampling_rate = 1.0;

        // Don't update variance
        private boolean fixed_variance = true;

        // Prior distribution
        private double prior_mean = 0.0;
        private double prior_deviation = 1.0;

        // Optimization strategy
        private Optimization optimization = null;

        private Builder() {}

        public Builder numSamples(int num_samples) {
            this.num_samples = num_samples;

            return this;
        }

        public Builder resamplingRate(double resampling_rate) {
            this.resampling_rate = resampling_rate;

            return this;
        }

        public Builder fixedVariance(boolean fixed_variance) {
            this.fixed_variance = fixed_variance;

            return this;
        }

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

        public GaussianDensity build() {
            if(null == optimization)
                throw new RuntimeException("DUMBASS!!! - No optimization strategy defined");

            return new GaussianDensity(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Variational load(JSONObject config) throws JSONException {
        return builder()
                .numSamples(config.getInt("num samples"))
                .resamplingRate(config.getDouble("resampling rate"))
                .fixedVariance(config.getBoolean("fixed variance"))
                .priorMean(config.getDouble("prior mean"))
                .priorDeviation(config.getDouble("prior deviation"))
                .optimization(Optimization.load(config.getJSONObject("optimization")))
                .build();
    }

    private final Builder config;

    private GaussianDensity(Builder config) { this.config = config; }

    private class Density implements Variational.Density {

        private int dimensions;
        private Random random;

        private double[][] samples;
        private int current_sample;

        private double[] value;

        private Optimization.Instance optimizer;

        private double[] parameters;
        private double[] gradient;

        private Density(int dimensions, Random random) {
            this.dimensions = dimensions;
            this.random = random;

            value = new double[dimensions];
            Arrays.fill(value, 0.0);

            samples = new double[config.num_samples][dimensions];

            for(int sample = 0; sample < config.num_samples; ++sample)
                for(int dim = 0; dim < dimensions; ++dim)
                    samples[sample][dim] = random.nextGaussian();

            parameters = new double[2 * dimensions];
            gradient = new double[2 * dimensions];

            initialize();
        }

        @Override
        public int size() { return dimensions; }

        @Override
        public int numSamples() { return config.num_samples; }

        @Override
        public void nextSample() {
            current_sample = random.nextInt(config.num_samples);

            if(random.nextDouble() < config.resampling_rate)
                for(int dim = 0; dim < dimensions; ++dim)
                    samples[current_sample][dim] = random.nextGaussian();

            for(int dim = 0; dim < dimensions; ++dim)
                value[dim] = parameters[dim] + (samples[current_sample][dim] * parameters[dimensions + dim]);
        }

        @Override
        public double[] value() { return value; }

        @Override
        public double[] mean() {
            double[] mean = new double[dimensions];
            System.arraycopy(parameters, 0, mean, 0, dimensions);

            return mean;
        }

        @Override
        public void train(double[] jacobian) {
            for(int dim = 0; dim < dimensions; ++dim) {
                gradient[dim] += jacobian[dim];

                if(!config.fixed_variance)
                    gradient[dimensions + dim] += jacobian[dim] * samples[current_sample][dim];
            }
        }

        @Override
        public void initialize() {

            // Initialize parameters and gradient
            Arrays.fill(parameters, 0, dimensions, config.prior_mean);
            Arrays.fill(parameters, dimensions, parameters.length, config.prior_deviation);

            // Initialize learner
            optimizer = config.optimization.instance(parameters.length);
        }

        @Override
        public void update() {

            // Compute regularization term
            for(int dim = 0; dim < dimensions; ++dim) {
                double variance = config.prior_deviation * config.prior_deviation;
                double deviation = parameters[dimensions + dim];

                gradient[dim] += (config.prior_mean - parameters[dim]) / variance;

                if(!config.fixed_variance)
                    gradient[dimensions + dim] += (1.0 / deviation) - (deviation / variance);
            }

            // Update parameters
            optimizer.update(parameters, gradient);

            // Reset gradient
            Arrays.fill(gradient, 0.0);
        }

        @Override
        public void clear() { Arrays.fill(gradient, 0.0); }
    }

    @Override
    public Density density(int dimensions, Random random) {
        return this.new Density(dimensions, random);
    }

    @Override
    public String name() {
        return "Gaussian Distribution";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("class", getClass().getSimpleName())
                .put("num samples", config.num_samples)
                .put("resampling rate", config.resampling_rate)
                .put("fixed variance", config.fixed_variance)
                .put("prior mean", config.prior_mean)
                .put("prior deviation", config.prior_deviation)
                .put("optimization", config.optimization.serialize());
    }
}
