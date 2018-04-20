package bam.algorithms.alt.new_optimization;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * An implementation of the AdaGrad optimization strategy
 */
public class AdaGrad implements Optimizer {

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
                    second_moment[i] += gradient[i] * gradient[i];
                    parameters[i] += learning_rate * gradient[i] / (Math.sqrt(second_moment[i]) + offset);
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
