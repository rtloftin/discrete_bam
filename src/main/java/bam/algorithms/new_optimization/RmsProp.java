package bam.algorithms.new_optimization;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Implements the RMSProp update strategy.
 */
public class RmsProp implements Optimizer {

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
    public Parameters parameters(int size, Consumer<double[]> initializer) {
        double[] parameters = new double[size];
        double[] second_moment = new double[size];

        return new Parameters() {

            { initialize(); }

            @Override
            public void initialize() {
                initializer.accept(parameters);
                Arrays.fill(second_moment, 0.0);
            }

            @Override
            public void update(double[] gradient) {
                for(int i=0; i < parameters.length; ++i) {
                    second_moment[i] = average_ratio * second_moment[i]
                            + (1.0 - average_ratio) * gradient[i] * gradient[i];
                    parameters[i] += learning_rate * gradient[i] / Math.sqrt(second_moment[i] + offset);
                }
            }

            @Override
            public double[] value() {
                return parameters;
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
                .put("learning rate", learning_rate)
                .put("average ratio", average_ratio)
                .put("offset", offset);
    }
}
