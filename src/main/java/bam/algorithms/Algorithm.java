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
    default JSONObject serialize() throws JSONException {
        return new JSONObject().put("name", name());
    }
}
