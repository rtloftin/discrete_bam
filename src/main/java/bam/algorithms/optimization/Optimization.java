package bam.algorithms.optimization;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a particular algorithm
 * for performing gradient ascent.
 * assumes that parameters are
 * accumulated in a flat vector.
 * Each instance is associated with
 * a single parameter vector. batch
 * accumulation and scaling of sample
 * gradients should be done externally
 * to an object of this class.
 *
 * Created by Tyler on 5/2/2017.
 */
public interface Optimization {

    interface Instance {

        /**
         * Updates the parameters of this model.
         *
         * @param parameters the parameters to update
         * @param gradient the next gradient sample
         */
        void update(double[] parameters, double[] gradient);
    }

    /**
     * Gets a new instance of this optimization strategy.
     *
     * @param num_parameters the number of parameters that need to be updated
     * @return the optimization strategy instance
     */
    Instance instance(int num_parameters);

    /**
     * Gets the name of this optimization strategy.
     *
     * @return the name of this optimization strategy
     */
    String name();

    /**
     * Gets a JSON representation of this optimization strategy.
     *
     * @return a json representation of this optimization strategy
     * @throws JSONException
     */
    default JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("class", getClass());
    }

    /**
     * Loads an implementation of this interface
     * from its JSON representation.
     *
     * @param config the JSON representation of the object
     * @return an instance defined by the JSON representation
     * @throws JSONException
     */
    static Optimization load(JSONObject config) throws JSONException {
        String className = config.getString("class");

        if(className.equals(GradientAscent.class.getSimpleName()))
            return GradientAscent.load(config);
        else if(className.equals(Momentum.class.getSimpleName()))
            return Momentum.load(config);
        else if(className.equals(AdaGrad.class.getSimpleName()))
            return AdaGrad.load(config);
        else if(className.equals(RmsProp.class.getSimpleName()))
            return RmsProp.load(config);
        else if(className.equals(Adam.class.getSimpleName()))
            return Adam.load(config);

        throw new RuntimeException("Unknown Implementation of 'Optimization' requested");
    }
}
