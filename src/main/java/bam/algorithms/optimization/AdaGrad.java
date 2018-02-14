package bam.algorithms.optimization;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * An implementation of the AdaGrad optimization strategy
 *
 * Created by Tyler on 5/2/2017.
 */
public class AdaGrad implements Optimization {

    private double learning_rate;
    private double offset;

    private AdaGrad(double learning_rate, double offset) {
        this.learning_rate = learning_rate;
        this.offset = offset;
    }

    public static AdaGrad with(double learning_rate, double offset) {
        return new AdaGrad(learning_rate, offset);
    }

    @Override
    public Optimization.Instance instance(int num_parameters) {
        final double[] second_moment = new double[num_parameters];
        Arrays.fill(second_moment, 0.0);

        return (double[] parameters, double[] gradient) -> {
                for(int i=0; i < parameters.length; ++i) {
                    second_moment[i] += gradient[i] * gradient[i];
                    parameters[i] += learning_rate * gradient[i] / (Math.sqrt(second_moment[i]) + offset);
                }
            };
    }

    @Override
    public String name() {
        return "AdaGrad";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("learning rate", learning_rate)
                .put("offset", offset);
    }
}
