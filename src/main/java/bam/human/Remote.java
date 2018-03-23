package bam.human;

import bam.algorithms.Algorithm;
import bam.domains.Environment;
import bam.domains.farm_world.FarmWorld;
import bam.domains.gravity_world.GravityWorld;
import bam.domains.grid_world.GridWorld;
import bam.human.domains.RemoteFarmWorld;
import bam.human.domains.RemoteGravityWorld;
import bam.human.domains.RemoteGridWorld;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a single instance of an environment
 * that is being visualized and interacted with
 * by a remote client.  Objects implementing this
 * interface maintain the current state of the
 * environment, and have access to the agent that
 * is operating in this environment.  All calls to
 * the agent that directly depend on the environment
 * should go through this interface, while those that
 * do not (data integration, serialization and logging)
 * should be handled separately by the session.
 */
public interface Remote {

    /**
     * An object that allows us to construct new
     * remote environment instances based on a
     * configuration message provided by the client.
     */
    interface Factory {

        /**
         * Gets a remote environment instance wrapping an instance of the learning
         * algorithm provided, and starting with the initial state provided.
         *
         * @param algorithm the learning algorithm for this instance to use
         * @param initial the initial state configuration of the environment
         * @return a new remote environment
         * @throws JSONException
         */
        Remote build(Algorithm algorithm, JSONObject initial) throws JSONException;

        /**
         * Returns a JSON representation of the environment
         * backing this factory instance.
         *
         * @return a JSON representation of the environment
         * @throws JSONException
         */
        JSONObject serialize() throws JSONException;
    }

    /**
     * Gets a remote simulation factory which constructs remote
     * instances based on an environment defined by a JSON object.
     *
     * @param config a JSON representation of the environment
     * @return a factory object that builds remote simulations using the environment
     * @throws JSONException
     */
    static Factory load(JSONObject config) throws JSONException {
        String className = config.getString("class");

        if(className.equals(GridWorld.class.getSimpleName()))
           return RemoteGridWorld.load(config);
        else if(className.equals(GravityWorld.class.getSimpleName()))
            return RemoteGravityWorld.load(config);
        else if(className.equals(FarmWorld.class.getSimpleName()))
            return RemoteFarmWorld.load(config);

        throw new RuntimeException("No Remote container available for this environment");
    }

    /**
     * Updates the agent's knowledge state to incorporate
     * all the data currently available, and returns a JSON
     * representation of the agent's current knowledge state.
     */
    JSONObject integrate() throws JSONException;

    /**
     * Sets the current task of the environment, and
     * informs the agent that the task has changed.
     *
     * @param task the JSON representation of the task
     * @throws JSONException if the JSON object isn't formatted correctly
     */
    void setTask(JSONObject task) throws JSONException;

    /**
     * Directly resets the state to one chosen by the client.
     *
     * @param state the JSON representation of the desired state
     * @throws JSONException if the JSON object isn't formatted correctly
     */
    void setState(JSONObject state) throws JSONException;

    /**
     * Randomly resets the state, based on the current task.
     */
    void resetState();

    /**
     * Takes an action selected by the client, and
     * shows that action to the learning agent, updates
     * the state and shows that transition to the learning
     * agent as well.  Whether the agent interprets this
     * as a teacher action depends on the agent's settings.
     *
     * @param action the JSON representation of the client's action
     * @throws JSONException if the JSON object isn't formatted correctly
     */
    void takeAction(JSONObject action) throws JSONException;

    /**
     * Takes an action from the learning agent's current
     * policy for the current task.  May block if data
     * integration running in another thread is not complete.
     * Updates the state and shows the transition to the
     * learning agent as well.
     */
    void takeAction();

    /**
     * Gets the maximum episode length
     * for this environment.
     *
     * @return the maximum episode length
     */
    int getDepth();

    /**
     * Gets a JSON representation of the list
     * of tasks available in this environment.
     *
     * @return the list of tasks as a JSON array
     * @throws JSONException if there is an error constructing the JSON array
     */
    JSONArray getTasks() throws JSONException;

    /**
     * Gets a JSON representation of the visual layout of the
     * environment that will be displayed to the user.  This
     * layout doesn't change depending on the current state,
     * only on the current task being taught.
     *
     * @return the JSON representation of the current layout
     * @throws JSONException if there is an error constructing the JSON object
     */
    JSONObject getLayout() throws JSONException;

    /**
     * Gets a JSON representation of the current state
     * of the environment.
     *
     * @return the JSON representation of the current state
     * @throws JSONException if there is an error constructing the JSON object
     */
    JSONObject getState() throws JSONException;
}
