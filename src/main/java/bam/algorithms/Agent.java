package bam.algorithms;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a multitask learning
 * algorithm that can incorporate
 * feedback, demonstrations and
 * direct exploration.  Provides
 * methods for getting action and
 * visualizations, and for synchronizing
 * learning updates.
 *
 * Created by Tyler on 8/26/2017.
 */
public interface Agent extends Policy {

    /**
     * Sets the current task being taught,
     * with which all subsequent data
     * will be associated.  The policy
     * implemented by the agent will
     * be the policy for the current task.
     *
     * @param name the name of the current task
     */
    void task(String name);

    /**
     * Gives the algorithms an observation
     * of a demonstrated action.
     *
     * @param action the demonstrated action
     */
    void observe(TeacherAction action);

    /**
     * Gives the algorithms an observation
     * of a teacher's feedback.
     *
     * @param feedback the teacher's feedback
     */
    void observe(TeacherFeedback feedback);

    /**
     * Gives the algorithms an observation
     * of a state transition.
     *
     * @param transition the state transition
     */
    void observe(StateTransition transition);

    /**
     * Computes one update of the agent's internal
     * representation.  Each update should do a fixed
     * amount of work, and should incorporate all of the
     * data the agent has received.
     */
    Behavior integrate();

    /**
     * WE NEED TO MAKE THE VISUALIZATION INTERFACE A LITTLE MORE CONSISTENT
     *
     * Gets any available visualizations
     * of the algorithms's current knowledge state.
     *
     * @return a list of available visualizations
     */
    default List<Visualization> visualizations() {
        return new LinkedList<>();
    }
}
