package bam.algorithms;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a specific configuration of a learning
 * algorithm, and allows us to build learning agents
 * which use this algorithm.
 *
 * Created by Tyler on 9/5/2017.
 */
public interface Algorithm {

    /**
     * Gets a new agent which uses this learning
     * algorithm. This agent uses the hypothesis
     * space specified by the representation object provided.
     *
     * @param representation the learning problem specification
     * @return a new agent
     */
    Agent agent(Representation representation);

    /**
     * Gets the name of the algorithm.
     *
     * @return the name of the algorithm
     */
    String name();

    /**
     * Gets a JSON representation of the configuration
     * options for this algorithm.
     *
     * @return a JSON object representing this algorithm
     * @throws JSONException if something goes wrong during serialization
     */
    JSONObject serialize() throws JSONException;

    /**
     * Loads an implementation of this interface
     * from its JSON representation.
     *
     * @param config the JSON representation of the object
     * @return an instance defined by the JSON representation
     * @throws JSONException
     */
    static Algorithm load(JSONObject config) throws JSONException {
        String className = config.getString("class");

        if(className.equals(BAM.class.getSimpleName()))
            return BAM.load(config);
        else if(className.equals(ModelBased.class.getSimpleName()))
            return ModelBased.load(config);
        else if(className.equals(MLIRL.class.getSimpleName()))
            return MLIRL.load(config);
        else if(className.equals(SERD.class.getSimpleName()))
            return SERD.load(config);
        else if(className.equals(Cloning.class.getSimpleName()))
            return Cloning.load(config);

        throw new RuntimeException("Unknown Implementation of 'Algorithm' requested");
    }
}
