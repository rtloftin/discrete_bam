package bam.algorithms.action;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * This class represents an action model that is invariant
 * with respect to the scale of the action values
 *
 * TODO: Recheck the math on this, can also stand some improvements in implementation
 *
 * Created by Tyler on 9/11/2017.
 */
public class NormalizedActionModel implements ActionModel {

    private final double beta;

    private NormalizedActionModel(double beta) {
        this.beta = beta;
    }

    private double mean(double[] values) {
        double mean = 0.0;

        for(int i=0; i < values.length; ++i)
            mean += values[i];

        return mean / (double) values.length;
    }

    private double[] deltas(double[] values, double mean) {
        double[] deltas = new double[values.length];

        for(int i=0; i < values.length; ++i)
            deltas[i] = values[i] - mean;

        return deltas;
    }

    private double variance(double[] deltas) {
        double variance = 0.0;

        for(int i=0; i < deltas.length; ++i)
            variance += deltas[i] * deltas[i];

        if(0 >= variance)
            return 1.0;

        return variance / (double) deltas.length;
    }

    private double[] utilities(double[] values, double deviation) {
        double[] utilities = new double[values.length];

        for(int i=0; i < values.length; ++i)
            utilities[i] =  values[i] / deviation;

        return utilities;
    }

    private double[] advantages(double[] distribution, double[] values) {
        double[] advantages = new double[distribution.length];
        double average = 0.0;

        for(int i=0; i < distribution.length; ++i)
            average += distribution[i] * values[i];

        for(int i=0; i < distribution.length; ++i)
            advantages[i] = values[i] - average;

        return advantages;
    }

    private double[] distribution(double[] values) {
        double[] policy = new double[values.length];
        double partition = 0.0;

        for(int i=0; i < values.length; ++i) {
            policy[i] = Math.exp(beta * (values[i] - values[0]));
            partition += policy[i];
        }

        if(!Double.isFinite(partition) || 0 == partition)
            throw new RuntimeException("Normalized Action Model: Infinity, NaN, or Zero encountered, values: "
                    + Arrays.toString(values));

        for(int i=0; i < values.length; ++i)
            policy[i] /= partition;

        return policy;
    }

    private double[] jacobian(double[] deltas, double variance) {
        double[] jacobian = new double[deltas.length];
        double scale = 1.0 / (variance * (double) deltas.length);
        double total = 0.0;

        for(int i=0; i < deltas.length; ++i) {
            jacobian[i] = scale * deltas[i];
            total += deltas[i];
        }

        scale *= total / (double) deltas.length;

        for(int i=0; i < deltas.length; ++i)
            jacobian[i] -= scale;

        return jacobian;
    }

    public static NormalizedActionModel beta(double beta) {
        return new NormalizedActionModel(beta);
    }

    public static NormalizedActionModel get() {
        return new NormalizedActionModel(1.0);
    }

    @Override
    public double[] policy(double[] values) {

        // Compute the differences between the mean value and each individual value
        double[] deltas = deltas(values, mean(values));

        // Compute the deviation-normalized utilities
        double[] utilities = utilities(values,  Math.sqrt(variance(deltas)));

        // Get the boltzmann policy under the normalize utilities
        return distribution(utilities);
    }

    @Override
    public void gradient(int action, double[] values, double[] gradient, double weight) {

        // Compute the differences between the mean value and each individual value
        double[] deltas = deltas(values, mean(values));

        // Compute variance and deviation
        double variance = variance(deltas);
        double deviation = Math.sqrt(variance);

        // Compute the normalized utilities
        double[] utilities = utilities(values, deviation);

        // Compute the action distribution
        double[] distribution = distribution(utilities);

        // Compute the advantages under this distribution
        double[] advantages = advantages(distribution, utilities);

        // Compute the jacobian of the deviation, and multiply times the action gradient
        double[] jacobian =jacobian(deltas, variance);

        for(int a = 0; a < jacobian.length; ++a) {
            jacobian[a] *= (1.0 - beta * advantages[action]);
            jacobian[a] -= beta * distribution[a] / deviation;

            if(action == a)
                jacobian[a] += beta / deviation;

            gradient[a] += weight * jacobian[a];
        }
    }

    /**
     * Gets the name of this action model.
     *
     * @return the name of this action model
     */
    @Override
    public String name() {
        return "Normalized Boltzmann";
    }

    /**
     * Gets a JSON representation of this model.
     *
     * @return a json representation of this model
     * @throws JSONException
     */
    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("beta", beta);
    }


}
