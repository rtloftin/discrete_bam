package bam.algorithms.action;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is now totally inconsistent with how we have defined the ActionModel interface.
 * I don't think we really need this class, since there is basically no context in which
 * we would actually assume that the teacher's actions are optimal.
 *
 * Created by Tyler on 7/28/2017.
 */
public class GreedyActionModel implements ActionModel {

    // The rate of suboptimal actions
    private final double epsilon;

    private GreedyActionModel(double epsilon) { this.epsilon = epsilon; }

    /**
     * Gets a GreedyActionModel
     * instance which never selects
     * suboptimal actions.
     *
     * @return a GreedyActionModel instance
     */
    public static GreedyActionModel get() {
        return new GreedyActionModel(0.0);
    }

    /**
     * Gets a GreedyActionModel
     * instance with the given
     * error rate.
     *
     * @param epsilon the error rate
     * @return a GreedyActionModel instance
     */
    public static GreedyActionModel epsilon(double epsilon) {
        return new GreedyActionModel(epsilon);
    }

    public static GreedyActionModel load(JSONObject config) throws JSONException {
        return epsilon(config.getDouble("epsilon"));
    }

    @Override
    public double[] policy(double[] values) {
        double max = -Double.MAX_VALUE;
        int num_max = 0;

        for(int a=0; a < values.length; ++a) {
            if (values[a] > max) {
                max = values[a];
                num_max = 1;
            } else if (values[a] == max)
                ++num_max;
        }

        double alpha = epsilon / values.length;
        double beta = alpha + (1.0 - epsilon) / num_max;

        double[] p = new double[values.length];

        for(int a=0; a < values.length; ++a)
            p[a] = (values[a] == max) ? beta : alpha;

        return p;
    }

    @Override
    public void gradient(int action, double[] values, double[] gradient, double weight) {
        double max = -Double.MAX_VALUE;

        for(int a=0; a < values.length; ++a)
            if(values[a] > max)
                max = values[a];

        // Not sure that this gradient is a good approximation, but we will never use this anyway
        for(int a=0; a < values.length; ++a)
            gradient[a] += weight * ((values[a] == max) ? (epsilon - 1.0) : -epsilon);

        gradient[action] += weight;
    }

    /**
     * Gets the name of this action model.
     *
     * @return the name of this action model
     */
    @Override
    public String name() {
        return "Greedy";
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
                .put("epsilon", epsilon);
    }
}
