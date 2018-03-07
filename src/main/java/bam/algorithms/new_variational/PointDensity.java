package bam.algorithms.new_variational;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A distribution with a Gaussian
 * prior, but represented by a single
 * point at its mean.
 *
 * Created by Tyler on 7/23/2017.
 */
public class PointDensity implements Density {

    private final double prior_mean;
    private final double prior_deviation;

    private PointDensity(double prior_mean, double prior_deviation) {
        this.prior_mean = prior_mean;
        this.prior_deviation = prior_deviation;
    }

    public static PointDensity with(double prior_mean, double prior_deviation) {
        return new PointDensity(prior_mean, prior_deviation);
    }

    public static PointDensity standard() {
        return new PointDensity(0.0, 1.0);
    }

    @Override
    public int numParameters(int dimensions) {
        return dimensions;
    }

    @Override
    public void initialize(double[] parameters) {
        Arrays.fill(parameters, prior_mean);
    }

    @Override
    public List<? extends Sample> sample(double[] parameters, Random random) {
        double[] value = new double[parameters.length];
        System.arraycopy(parameters, 0, value, 0, parameters.length);

        return List.of(new Sample() {
            @Override
            public double[] value() {
                return value;
            }

            @Override
            public void gradient(double[] weights, double[] gradient) {
                for(int i=0; i < value.length; ++i)
                    gradient[i] += weights[i];
            }
        });
    }

    @Override
    public void regularize(double[] parameters, double[] gradient, double weight) {
        double scale = weight / (prior_deviation * prior_deviation);

        for(int i=0; i < parameters.length; ++i)
            gradient[i] += scale * (prior_mean - parameters[i]);
    }

    @Override
    public double[] mean(double[] parameters) {
        double[] value = new double[parameters.length];
        System.arraycopy(parameters, 0, value,0, parameters.length);

        return value;
    }

    @Override
    public String name() {
        return "Point Estimate";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("prior mean", prior_mean)
                .put("prior deviation", prior_deviation);
    }
}
