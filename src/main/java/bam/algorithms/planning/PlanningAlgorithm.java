package bam.algorithms.planning;

import bam.Dynamics;
import bam.DynamicsModel;
import bam.Reward;
import bam.RewardMapping;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a generic planning
 * algorithm, which takes a specific
 * planning graph and returns a planner
 * which uses this algorithm on that graph.
 *
 * Created by Tyler on 8/29/2017.
 */
public interface PlanningAlgorithm {

    /**
     * Gets a planning instance based on the dynamics
     * and reward function provided.  Training signals
     * are ignored, as these are fixed.
     *
     * @param dynamics the transition dynamics
     * @param rewards the reward function
     * @return the planning module
     */
    default Planner planner(Dynamics dynamics, Reward rewards) {
        return planner(FixedGraph.of(dynamics, rewards));
    }

    /**
     * Gets a planner instance based
     * on the planning graph provided.
     *
     * @param graph the planning graph
     * @return the planner
     */
    Planner planner(PlanningGraph graph);

    /**
     * Gets the name of this planning algorithm.
     *
     * @return the name of this planning algorithm
     */
    String name();

    /**
     * Gets a JSON representation of this algorithm.
     *
     * @return a json representation of this algorithm
     * @throws JSONException
     */
    default JSONObject serialize() throws JSONException {
        return new JSONObject().put("name", name());
    }
}
