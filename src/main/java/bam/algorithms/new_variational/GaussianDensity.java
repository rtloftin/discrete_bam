package bam.algorithms.new_variational;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A variational model of a gaussian density.
 */
public class GaussianDensity implements Density {

    public static class Builder {

        // Number of samples to generate
        private int num_samples = 1;

        // Don't update variance
        private boolean fixed_variance = true;

        // Prior distribution
        private double prior_mean = 0.0;
        private double prior_deviation = 1.0;

        private Builder() {}

        public Builder numSamples(int num_samples) {
            this.num_samples = num_samples;

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

        public GaussianDensity build() {
            return new GaussianDensity(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Sampling method
    private final int num_samples;
    private final boolean fixed_variance;
    private final double prior_mean;
    private final double prior_deviation;

    private GaussianDensity(Builder config) {
        this.num_samples = config.num_samples;
        this.fixed_variance = config.fixed_variance;
        this.prior_mean = config.prior_mean;
        this.prior_deviation = config.prior_deviation;
    }

    @Override
    public int numParameters(int dimensions) {
        return 2 * dimensions;
    }

    @Override
    public void initialize(double[] parameters) {
        int dimensions = parameters.length / 2;

        for(int i=0; i < dimensions; ++i) {
            parameters[i] = prior_mean;
            parameters[i + dimensions] = prior_deviation;
        }
    }

    @Override
    public List<? extends Sample> sample(double[] parameters, Random random) {
        List<Sample> samples = new ArrayList<>(num_samples);
        int dimensions = parameters.length / 2;

        for(int sample = 0; sample < num_samples; ++sample) {
            double[] input = new double[dimensions];
            double[] output = new double[dimensions];

            for(int i=0; i < dimensions; ++i) {
                input[i] = random.nextGaussian();
                output[i] = (parameters[i + dimensions] * input[i]) + parameters[i];
            }

            samples.add(new Sample() {
                @Override
                public double[] value() {
                    return output;
                }

                @Override
                public void gradient(double[] weights, double[] gradient) {
                    for(int i=0; i < dimensions; ++i) {
                        gradient[i] += weights[i];

                        if(!fixed_variance)
                            gradient[i + dimensions] += weights[i] * input[i];
                    }
                }
            });
        }

        return samples;
    }

    @Override
    public void regularize(double[] parameters, double[] gradient, double weight) {
        int dimensions = parameters.length / 2;
        double variance = prior_deviation * prior_deviation;

        for(int i=0; i < dimensions; ++i) {
            gradient[i] += weight * (prior_mean - parameters[i]) / variance;

            if(!fixed_variance) {
                double deviation = parameters[i + dimensions];
                gradient[i + dimensions] += weight * ((1.0 / deviation) - (deviation / variance));
            }
        }
    }

    @Override
    public double[] mean(double[] parameters) {
        int dimensions = parameters.length / 2;
        double[] mean = new double[dimensions];

        System.arraycopy(parameters, 0, mean, 0, dimensions);

        return mean;
    }

    @Override
    public String name() {
        return "Gaussian Distribution";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("num samples", num_samples)
                .put("fixed variance", fixed_variance)
                .put("prior mean", prior_mean)
                .put("prior deviation", prior_deviation);
    }
}
