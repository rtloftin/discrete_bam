package bam.algorithms.action;


import java.util.Arrays;

/**
 * Created by Tyler on 8/20/2017.
 */
public class RandomActionModel implements ActionModel {

    private static RandomActionModel object = new RandomActionModel();

    private RandomActionModel() {}

    /**
     * Gets an instance of the RandomActionModel class.
     *
     * @return a RandomActionModel instance
     */
    public static RandomActionModel get() { return object; }

    @Override
    public double[] policy(double[] values) {
        double[] p = new double[values.length];
        Arrays.fill(p, 1.0 / values.length);

        return p;
    }

    @Override
    public void gradient(int action, double[] values, double[] gradient, double weight) {
        /* Gradient is always zero, so don't update the gradient buffer */
    }

    /**
     * Gets the name of this action model.
     *
     * @return the name of this action model
     */
    @Override
    public String name() {
        return "Random";
    }
}
