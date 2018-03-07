package bam.algorithms.planning;

import bam.algorithms.DynamicsModel;
import bam.algorithms.RewardMapping;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * This class integrates a dynamics model and
 * a reward mapping into a planning graph, which
 * includes methods for setting the intent and
 * getting the intent gradient.
 *
 * Created by Tyler on 5/13/2017.
 */
public class ModelGraph implements PlanningGraph {

    private DynamicsModel dynamics;
    private RewardMapping rewards;

    private double[] intent;
    private double[] gradient;

    private ModelGraph(DynamicsModel dynamics, RewardMapping rewards) {
        this.dynamics = dynamics;
        this.rewards = rewards;

        intent = new double[rewards.intentSize()];
        gradient = new double[rewards.intentSize()];
    }

    public static ModelGraph of(DynamicsModel dynamics, RewardMapping rewards) {
        return new ModelGraph(dynamics, rewards);
    }

    @Override
    public int depth() {
        return dynamics.depth();
    }

    @Override
    public int numNodes(int depth) { return dynamics.numStates(); }

    @Override
    public int numOptions(int depth, int node) { return dynamics.numActions(node); }

    @Override
    public int[] successors(int depth, int node, int option) {
        return dynamics.successors(node, option);
    }

    @Override
    public double[] transitions(int depth, int node, int option) {
        return dynamics.transitions(node, option);
    }

    @Override
    public void train(int depth, int start, int option, int end, double weight) {
        dynamics.train(start, option, end, weight);
    }

    @Override
    public double reward(int depth, int node) {
        return rewards.reward(node, intent);
    }

    @Override
    public void train(int depth, int node, double weight) {
        rewards.gradient(node, intent, weight, gradient);
    }

    /////////////
    // Intents //
    /////////////

    /**
     * Sets the intent vector, which sets the reward function
     *
     * @param intent the intent vector
     */
    public void setIntent(double[] intent) {
        System.arraycopy(intent, 0, this.intent, 0, rewards.intentSize());
        Arrays.fill(gradient, 0.0);
    }

    /**
     * Gives the ACCUMULATED gradient with respect to the intent
     * to the specified consumer, then resets the accumulator.
     *
     * @param target the consumer to which the gradient should be supplied.
     */
    public void intentGradient(Consumer<double[]> target) {
        target.accept(gradient);
        Arrays.fill(gradient, 0.0);
    }
}
