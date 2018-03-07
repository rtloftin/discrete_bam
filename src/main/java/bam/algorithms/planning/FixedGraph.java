package bam.algorithms.planning;

import bam.algorithms.Dynamics;
import bam.algorithms.Reward;

/**
 * Combines a set of transition dynamics and
 * a reward function into a planning graph.
 */
public class FixedGraph implements PlanningGraph {

    private Dynamics dynamics;
    private Reward rewards;

    private FixedGraph(Dynamics dynamics, Reward rewards) {
        this.dynamics = dynamics;
        this.rewards = rewards;
    }

    public static FixedGraph of(Dynamics dynamics, Reward rewards) {
        return new FixedGraph(dynamics, rewards);
    }

    @Override
    public int depth() { return dynamics.depth(); }

    @Override
    public int numNodes(int depth) { return dynamics.numStates(); }

    @Override
    public int numOptions(int depth, int node) { return dynamics.numActions(node); }

    @Override
    public int[] successors(int depth, int node, int option) { return dynamics.successors(node, option); }

    @Override
    public double[] transitions(int depth, int node, int option) { return dynamics.transitions(node, option); }

    @Override
    public void train(int depth, int start, int action, int end, double weight) { /* DOES NOTHING */ }

    @Override
    public double reward(int depth, int node) { return rewards.reward(node); }

    @Override
    public void train(int depth, int node, double weight) { /* DOES NOTHING */ }
}
