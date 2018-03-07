package bam.domains;

import bam.algorithms.Reward;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Represents a specific task defined
 * by a reward function and an initial
 * state distribution.
 *
 * Created by Tyler on 7/26/2017.
 */
public interface Task extends Reward {

    /**
     * Samples a state from the task's
     * initial state distribution.
     *
     * @param random a random number generator
     * @return the sampled state.
     */
    int initial(Random random);

    /**
     * Gets the name of this task.
     *
     * @return the name of this task
     */
    String name();

    /**
     * Gets a json representation of this task.
     *
     * @return a json representation of this task
     * @throws JSONException
     */
    default JSONObject serialize() throws JSONException {
        return new JSONObject().put("name", name());
    }
}
