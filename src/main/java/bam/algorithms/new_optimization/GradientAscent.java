package bam.algorithms.new_optimization;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implements the basic gradient ascent optimization strategy.
 */
public class GradientAscent implements Optimizer {

    private double learning_rate;

    private GradientAscent(double learning_rate) {
        this.learning_rate = learning_rate;
    }

    public static GradientAscent with(double learning_rate) {
        return new GradientAscent(learning_rate);
    }

    @Override
    public Parameters parameters(double[] initial) {
        double[] parameters = new double[initial.length];

        return new Parameters() {

            { initialize(); }

            @Override
            public void initialize() {
                System.arraycopy(initial, 0, parameters, 0, initial.length);
            }

            @Override
            public void update(double[] gradient) {
                for(int i=0; i < parameters.length; ++i)
                    parameters[i] += learning_rate * gradient[i];
            }

            @Override
            public double[] value() {
                return parameters;
            }
        };
    }

    @Override
    public String name() {
        return "Gradient Ascent";
    }

    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("learning rate", learning_rate);
    }
}
