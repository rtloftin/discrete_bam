package bam.algorithms.feedback;


/**
 * An advantage model based on the optimal
 * value under the current value function.
 *
 * Has no parameters, so we use a singleton.
 *
 * Created by Tyler on 8/28/2017.
 */
public class MaxAdvantage implements AdvantageModel {

    private static final MaxAdvantage object = new MaxAdvantage();

    private MaxAdvantage() {}

    /**
     * Gets the MaxAdvantage model.
     *
     * @return the MaxAdvantage model
     */
    public static MaxAdvantage get() {
        return object;
    }

    @Override
    public double advantage(int action, double[] values) {
        double max = -Double.MAX_VALUE;

        for(int a=0; a < values.length; ++a)
            if(values[a] > max)
                max = values[a];

        return values[action] - max;
    }

    @Override
    public void gradient(int action, double[] values, double[] gradient, double weight) {
        double max = -Double.MAX_VALUE;
        int num_max = 0;

        for(int a=0; a < values.length; ++a) {
            if (values[a] > max) {
                max = values[a];
                num_max = 1;
            }
            else if(values[a] == max)
                ++num_max;
        }

        double scale = weight / num_max;

        for(int a=0; a < values.length; ++a)
            if(values[a] == max)
                gradient[a] -= scale;

        gradient[action] += weight;
    }

    @Override
    public String name() {
        return "Max Advantage";
    }
}
