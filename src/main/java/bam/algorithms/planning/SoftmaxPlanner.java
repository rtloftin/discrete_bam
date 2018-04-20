package bam.algorithms.planning;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * A planning module that uses the
 * softmax function to average
 * over possible actions.
 *
 * Created by Tyler on 5/9/2017.
 */
public class SoftmaxPlanner implements Planner {

    public static PlanningAlgorithm algorithm() {
        return algorithm(1.0);
    }

    public static PlanningAlgorithm algorithm(final double beta) {
        return new PlanningAlgorithm() {
            @Override
            public Planner planner(PlanningGraph graph) {
                return new SoftmaxPlanner(graph, beta);
            }

            @Override
            public String name() {
                return "Softmax Planner";
            }

            @Override
            public JSONObject serialize() throws JSONException {
                return new JSONObject()
                        .put("name", name())
                        .put("class", SoftmaxPlanner.class.getSimpleName())
                        .put("beta", beta);
            }
        };
    }

    public static PlanningAlgorithm load(JSONObject config) throws JSONException {
        return algorithm(config.getDouble("beta"));
    }

    // Planning model
    private PlanningGraph dynamics;

    // Parameters
    private double beta; // inverse temperature

    // Data structures
    private double[][][] Q; // state-action value functions
    private double[][] V; // state value functions

    private double[][][] PI; // Boltzmann policy at each step

    private double[][][] DQ; // state-action backpropagation buffers -- need one for each depth
    private double[][] DV; // state backpropagation buffers -- need one for each depth

    private SoftmaxPlanner(PlanningGraph dynamics, double beta) {
        this.dynamics = dynamics;
        this.beta = beta;

        Q = new double[dynamics.depth() + 1][][];
        DQ = new double[dynamics.depth() + 1][][];

        V = new double[dynamics.depth()][];
        PI = new double[dynamics.depth()][][];
        DV = new double[dynamics.depth()][];

        for(int depth = 0; depth <= dynamics.depth(); ++depth) {
            int num_states = dynamics.numNodes(depth);

            Q[depth] = new double[num_states][];
            DQ[depth] = new double[num_states][];

            if(depth < dynamics.depth()) {
                V[depth] = new double[num_states];
                DV[depth] = new double[num_states];
                PI[depth] = new double[num_states][];
            }

            for(int state = 0; state < num_states; ++state) {
                int num_actions = dynamics.numOptions(depth, state);

                Q[depth][state] = new double[num_actions];
                DQ[depth][state] = new double[num_actions];

                if(depth < dynamics.depth())
                    PI[depth][state] = new double[num_actions];
            }
        }
    }

    /**
     * Gets the number of output states, that is, the
     * number of states at the top layer of the transition
     * model.  If layers are not defined, this will
     * be equal to the number of states in the model.
     * This will always be equal to the first dimension
     * of the Q-function array.
     *
     * @return the number of output states
     */
    @Override
    public int numStates() {
        return dynamics.numNodes(dynamics.depth());
    }

    /**
     * Gets the number of actions available
     * in the given output state.
     *
     * @param state
     * @return the number of available actions
     */
    @Override
    public int numActions(int state) {
        return dynamics.numOptions(dynamics.depth(), state);
    }

    /**
     * Computes and returns the state-action value function.
     * Also updates internal data structures necessary for
     * backpropagation.
     *
     * @return the state-action value function
     */
    @Override
    public double[][] values() {
        for(int depth = 0; depth <= dynamics.depth(); ++depth) {

            // Add rewards to Q function at this depth
            for(int state = 0; state < dynamics.numNodes(depth); ++state)
                for(int action = 0; action < dynamics.numOptions(depth, state); ++action)
                    Q[depth][state][action] = dynamics.reward(depth, state);

            // If this isn't the first iteration, do transition lookahead
            if(0 != depth) {
                for (int start = 0; start < dynamics.numNodes(depth); ++start)
                    for (int action = 0; action < dynamics.numOptions(depth, start); ++action) {
                        int[] next = dynamics.successors(depth, start, action);
                        double[] dist = dynamics.transitions(depth, start, action);

                        for (int end = 0; end < next.length; ++end)
                            Q[depth][start][action] += V[depth - 1][next[end]] * dist[end];
                    }
            }

            // Update PI and V
            if(dynamics.depth() != depth) {
                for (int state = 0; state < dynamics.numNodes(depth); ++state) {
                    double partition = 0.0;
                    double max = -Double.MAX_VALUE;
                    int num_max = 0;

                    for (int action = 0; action < dynamics.numOptions(depth, state); ++action) {
                        double value = Q[depth][state][action];
                        double exp = Math.exp(beta * value);

                        PI[depth][state][action] = exp;
                        partition += exp;

                        if (value == max)
                            ++num_max;
                        else if (value > max) {
                            max = value;
                            num_max = 1;
                        }
                    }

                    // Check for cases of double overflow
                    if (Double.isFinite(partition)) {

                        // If the partition is finite, normalize PI and take the log of V
                        for (int action = 0; action < dynamics.numOptions(depth, state); ++action)
                            PI[depth][state][action] /= partition;

                        V[depth][state] = Math.log(partition) / beta;
                    } else {

                        // If it is infinite, set V to be the true max, and assign probability only to maximal actions
                        double p = 1.0 / (double) num_max;

                        for (int action = 0; action < dynamics.numOptions(depth, state); ++action) {
                            if (Q[depth][state][action] != max)
                                PI[depth][state][action] = 0.0;
                            else
                                PI[depth][state][action] = p;
                        }

                        V[depth][state] = max;
                    }
                }
            }
        }

        // Return the uppermost layer of the Q function
        return Q[dynamics.depth()];
    }

    /**
     * Backpropagates the jacobian of the objective w.r.t the
     * state-action value function through the stochastic_planning process.
     * This must be called after the actionValues() method
     * has been called.
     *
     * @param jacobian the jacobian w.r.t. the value function
     */
    @Override
    public void train(double[][] jacobian) {

        // Copy jacobian
        for(int state = 0; state < dynamics.numNodes(dynamics.depth()); ++state)
            for(int action = 0; action < dynamics.numOptions(dynamics.depth(), state); ++action)
                DQ[dynamics.depth()][state][action] = jacobian[state][action];

        // Do reverse value iteration
        for(int depth = dynamics.depth(); depth >= 0; --depth) {

            // If we are past the first iteration, backpropagate transitions and propagate through transitions
            if(dynamics.depth() > depth) {

                // Initialize DV
                Arrays.fill(DV[depth], 0.0);

                for(int start = 0; start < dynamics.numNodes(depth + 1); ++start)
                    for(int action = 0; action < dynamics.numOptions(depth + 1, start); ++action) {
                        int[] next = dynamics.successors(depth + 1, start, action);
                        double[] dist = dynamics.transitions(depth + 1, start, action);

                        for(int end = 0; end < next.length; ++end) {
                            double prop = DQ[depth + 1][start][action] * dist[end];
                            double weight = prop * V[depth][next[end]];

                            // Backpropagate log transition
                            dynamics.train(depth + 1, start, action, next[end], weight);

                            // Backpropagate through transition
                            DV[depth][next[end]] += prop;
                        }
                    }

                // Update DQ from DV
                for(int state = 0; state < dynamics.numNodes(depth); ++state)
                    for(int action = 0; action < dynamics.numOptions(depth, state); ++action)
                        DQ[depth][state][action] = PI[depth][state][action] * DV[depth][state];
            }

            // Backpropagate reward gradient
            for(int state = 0; state < dynamics.numNodes(depth); ++state) {
                double reward_weight = 0.0;

                for(int action = 0; action < dynamics.numOptions(depth, state); ++action)
                    reward_weight += DQ[depth][state][action];

                dynamics.train(depth, state, reward_weight);
            }
        }
    }
}
