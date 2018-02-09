package bam.algorithms.feedback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An advantage model based on the
 * mean under the softmax mean.
 *
 * When the softmax mean is undefined for a state,
 * resorts to the max advantage model instead,
 * which is the limiting case of the Boltzmann model.
 *
 * We use a builder pattern in case we wish to extend
 * this model in the future.
 *
 * Created by Tyler on 8/28/2017.
 */
public class SoftmaxAdvantage implements AdvantageModel {

    // A max advantage model to be used as a fallback
    private static MaxAdvantage max_advantage = MaxAdvantage.get();

    // The teacher's greediness
    private double beta;

    private SoftmaxAdvantage(double beta) {
        this.beta = beta;
    }

    /**
     * Returns a SoftmaxAdvantage with a
     * temperature of 1.0
     *
     * @return a SoftmaxAdvantage instance
     */
    public static SoftmaxAdvantage get() {
        return new SoftmaxAdvantage(1.0);
    }

    /**
     * Returns a SoftmaxAdvantage with
     * temperature beta
     *
     * @param beta the temperature
     * @return  a SoftmaxAdvantage instance
     */
    public static SoftmaxAdvantage beta(double beta) {
        return new SoftmaxAdvantage(beta);
    }

    @Override
    public double advantage(int action, double[] values) {
        double partition = 0.0;

        for(int a=0; a < values.length; ++a)
            partition += Math.exp(beta * (values[a] - values[0]));

        if(Double.isFinite(partition))
            return values[action] - Math.log(partition / values.length);

        return max_advantage.advantage(action, values);
    }

    @Override
    public void gradient(int action, double[] values, double[] gradient, double weight) {
        double partition = 0.0;

        for(int a=0; a < values.length; ++a)
            partition += Math.exp(beta * (values[a] - values[0]));

        if(Double.isFinite(partition)) {
            double scale = weight / partition;

            for(int a=0; a < values.length; ++a)
                gradient[a] -= scale * Math.exp(beta * (values[a] - values[0]));

            gradient[action] += weight;
        }
        else
            max_advantage.gradient(action, values, gradient, weight);
    }

    @Override
    public String name() {
        return "Softmax Advantage";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject().put("name", name()).put("beta", beta);
    }
}
