package bam.algorithms.feedback;


/**
 * An advantage model which uses
 * the advantage relative to the mean
 * under the uniform policy.
 *
 * The class has no parameters, so use
 * a singleton.
 *
 * Created by Tyler on 8/28/2017.
 */
public class MeanAdvantage implements AdvantageModel {

    private static final MeanAdvantage object = new MeanAdvantage();

    private MeanAdvantage() {}

    /**
     * Gets the MeanAdvantage model.
     *
     * @return the MeanAdvantage model
     */
    public static MeanAdvantage get() {
        return object;
    }

    @Override
    public double advantage(int action, double[] values) {
        double mean = 0.0;

        for(int a=0; a < values.length; ++a)
            mean += values[a];

        mean /= values.length;

        return values[action] - mean;
    }

    @Override
    public void gradient(int action, double[] values, double[] gradient, double weight) {
        double scale = weight / values.length;

        for(int a=0; a < values.length; ++a)
            gradient[a] -= scale;

        gradient[action] += weight;
    }

    @Override
    public String name() {
        return "Mean Advantage";
    }
}
