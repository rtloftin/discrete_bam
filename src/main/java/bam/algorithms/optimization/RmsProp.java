package bam.algorithms.optimization;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Implements the RMSProp update strategy.
 *
 * Created by Tyler on 5/2/2017.
 */
public class RmsProp implements Optimization {

    private double learning_rate;
    private double average_ratio;
    private double offset;

    private RmsProp(double learning_rate, double average_ratio, double offset) {
        this.learning_rate = learning_rate;
        this.average_ratio = average_ratio;
        this.offset = offset;
    }

    public static RmsProp with(double learning_rate, double average_ratio, double offset) {
        return new RmsProp(learning_rate, average_ratio, offset);
    }

    @Override
    public Optimization.Instance instance(int num_parameters) {
        final double[] second_moment = new double[num_parameters];
        Arrays.fill(second_moment, 0.0);

        return (double[] parameters, double[] gradient) -> {
                for(int i=0; i < parameters.length; ++i) {
                    second_moment[i] = average_ratio * second_moment[i]
                            + (1.0 - average_ratio) * gradient[i] * gradient[i];
                    parameters[i] += learning_rate * gradient[i] / Math.sqrt(second_moment[i] + offset);
                }
            };
    }

    @Override
    public String name() {
        return "RmsProp";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("bam.deprecated.learning rate", learning_rate)
                .put("average ratio", average_ratio)
                .put("offset", offset);
    }
}
