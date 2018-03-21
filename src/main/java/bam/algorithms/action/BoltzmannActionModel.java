package bam.algorithms.action;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tyler on 8/20/2017.
 */
public class BoltzmannActionModel implements ActionModel {

    // The temperature parameter for the action distribution
    private double beta;

    private BoltzmannActionModel(double beta) { this.beta = beta; }

    /**
     * Gets a BoltzmannActionModel
     * instance with a temperature of 1.0.
     *
     * @return a BoltzmannActionModel instance
     */
    public static BoltzmannActionModel get() {
        return new BoltzmannActionModel(1.0);
    }

    /**
     * Gets a BoltzmannActionModel
     * instance with temperature beta.
     *
     * @param beta the temperature
     * @return a BoltzmannActionModel instance
     */
    public static BoltzmannActionModel beta(double beta) {
        return new BoltzmannActionModel(beta);
    }

    public static BoltzmannActionModel load(JSONObject config) throws JSONException {
        return beta(config.getDouble("beta"));
    }

    @Override
    public double[] policy(double[] values) {
        double[] p = new double[values.length];
        double partition = 0.0;
        double max = -Double.MAX_VALUE;
        int num_max = 0;

        for(int a=0; a < values.length; ++a) {
            p[a] = Math.exp(beta * (values[a] - values[0]));
            partition += p[a];

            if(values[a] > max) {
                max = values[a];
                num_max = 1;
            }
            else if(values[a] == max)
                ++num_max;
        }

        if(Double.isInfinite(partition)) {
            double common = 1.0 / num_max;

            for(int action = 0; action < values.length; ++action)
                p[action] = (values[action] == max) ? common : 0.0;
        }
        else if(0 == partition) {
            double common = 1.0 / values.length;

            for(int action = 0; action < values.length; ++action)
                p[action] = common;
        }
        else {
            for(int action = 0; action < values.length; ++action)
                p[action] /= partition;
        }

        return p;
    }

    @Override
    public void gradient(int action, double[] values, double[] gradient, double weight) {
        double[] p = policy(values);

        weight *= beta;

        for(int a=0; a < values.length; ++a)
            gradient[a] -= weight * p[a];

        gradient[action] += weight;
    }

    /**
     * Gets the name of this action model.
     *
     * @return the name of this action model
     */
    @Override
    public String name() {
        return "Boltzmann";
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
                .put("class", getClass().getSimpleName())
                .put("beta", beta);
    }
}
