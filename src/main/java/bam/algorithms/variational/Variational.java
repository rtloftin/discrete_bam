package bam.algorithms.variational;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;


/**
 * An interface for working with an
 * arbitrary variational model.  Such a model
 * defines a prior distribution, as well
 * as a sampling scheme.
 *
 * Created by Tyler on 5/9/2017.
 */
public interface Variational {

    interface Density {

        /**
         * Gets the number of output dimensions.
         *
         * @return the number of dimensions
         */
        int size();

        /**
         * Gets the maximum number
         * of distinct samples the model
         * will maintain.  The learner
         * should not use any more samples
         * in computing each gradient, as
         * further samples would be redundant.
         *
         * @return the maximum number of output samples
         */
        int numSamples();

        /**
         * Requests that the model return
         * a different output sample. whether
         * this sample is new or an existing sample
         * is entirely up to the model.
         */
        void nextSample();


        /**
         * Gets the value buffer for the model.
         *
         * @return the output buffer
         */
        double[] value();

        /**
         * Gets the mean vector under
         * the current distribution.
         *
         * @return the mean of the distribution
         */
        default double[] mean() {
            double[] mean = new double[size()];
            Arrays.fill(mean, 0.0);

            for(int sample = 0; sample < numSamples(); ++sample) {

                // generate next sample
                nextSample();

                // Get sample value
                double[] val = value();

                // update mean
                for(int i=0; i < mean.length; ++i)
                    mean[i] += val[i];
            }

            for(int i=0; i < mean.length; ++i)
                mean[i] /= (double) numSamples();

            return mean;
        }

        /**
         * Backpropagates the Jacobian of the loss
         * function in terms of the vector.
         *
         * @param jacobian the jacobian with respect to the vector
         */
        void train(double[] jacobian);

        /**
         * Initializes the density parameters
         */
        void initialize();

        /**
         * Updates the parameters of the density with
         * respect to the current accumulated gradient.
         */
        void update();

        /**
         * Erases all gradients from the density since the last update.
         */
        void clear();
    }

    /**
     * Gets an instance of this variational density
     * with the specified dimensionality.
     *
     * @param dimensions the dimensionality of the random vector
     * @param random the random number source used to generate samples
     * @return a variational distribution
     */
    Density density(int dimensions, Random random);

    /**
     * Gets the name of this variational model.
     *
     * @return the name of this variational model
     */
    String name();

    /**
     * Gets a JSON representation of this model.
     *
     * @return a json representation of this model
     * @throws JSONException
     */
    default JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("class", getClass().getSimpleName());
    }

    /**
     * Loads an implementation of this interface
     * from its JSON representation.
     *
     * @param config the JSON representation of the object
     * @return an instance defined by the JSON representation
     * @throws JSONException
     */
    static Variational load(JSONObject config) throws JSONException {
        String className = config.getString("class");

        if(className.equals(PointDensity.class.getSimpleName()))
            return PointDensity.load(config);
        else if(className.equals(GaussianDensity.class.getSimpleName()))
            return GaussianDensity.load(config);

        throw new RuntimeException("Unknown Implementation of 'Variational' requested");
    }
}
