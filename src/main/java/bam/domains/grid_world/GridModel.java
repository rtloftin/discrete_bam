package bam.domains.grid_world;

import bam.algorithms.DynamicsModel;
import bam.algorithms.optimization.Optimization;
import bam.domains.NavGrid;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;

/**
 * A learned model represented by
 * and occupancy map.
 *
 * Warning: this class is not thread safe.
 *
 * Created by Tyler on 10/9/2017.
 */
class GridModel implements DynamicsModel {

    // private final double alpha = 1.0; // Figure the prior out, will also need for gravity world too
    // private final double beta = 5.0;

    private final double prior = -3.0; // need a better way of setting this

    // The underlying navigation grid
    private final NavGrid grid;

    // Number of states and actions, and planning depth
    private final int num_states;
    private final int num_actions;
    private final int depth;

    // Output buffers
    private final int[] one_successor = new int[1];
    private final int[] two_successors = new int[2];

    private final double[] one_transition = new double[]{ 1.0 };
    private final double[] two_transitions = new double[2];

    // Parameters
    private double[] parameters;
    private double[] gradient;

    // Parameter optimizer
    private Optimization.Instance optimizer = null;

    // Computes the probability that a state is occupied
    private double occupied(int index) {
        return 1.0 / (1.0 + Math.exp(-parameters[index]));
    }

    GridModel(NavGrid grid, int depth) {
        this.grid = grid;
        this.depth = depth;

        num_states = grid.numCells();
        num_actions = grid.numMoves();

        parameters = new double[num_states];
        gradient = new double[num_states];
    }

    @Override
    public int numStates() { return num_states; }

    @Override
    public int numActions(int state) { return num_actions; }

    @Override
    public int depth() { return depth; }

    @Override
    public int[] successors(int state, int action) {
        int next = grid.next(state, action);

        if(next == state) {
            one_successor[0] = state;
            return one_successor;
        }

        two_successors[0] = state;
        two_successors[1] = next;

        return two_successors;
    }

    @Override
    public double[] transitions(int state, int action) {
        int next = grid.next(state, action);

        if(next == state)
            return one_transition;

        double collision = occupied(next);

        two_transitions[0] = collision;
        two_transitions[1] = 1.0 - collision;

        return two_transitions;
    }

    @Override
    public void initialize(Optimization optimization) {
        Arrays.fill(parameters, prior);
        Arrays.fill(gradient, 0.0);

        optimizer = optimization.instance(parameters.length);
    }

    @Override
    public void train(int start, int action, int end, double weight) {
        int next = grid.next(start, action);

        if (next != start) { // If this action leads to a self transition, it tells us nothing
            double collision = occupied(next);

            if (start == end) { // Collision
                gradient[next] += weight * (1.0 - collision);
            } else if (next == end) { // No collision
                gradient[next] -= weight * collision;
            }
        }
    }

    @Override
    public void update() {

        // Check if learner was initialized
        if (null == optimizer)
            throw new RuntimeException("Optimization algorithm not initialized");

        /* for (int state = 0; state < grid.numStates(); ++state) {
               double p = occupied(state);
               gradient[state] += alpha * (1.0 - p) - beta * p;
           } */

        // Perform update
        optimizer.update(parameters, gradient);

        // Reset gradient
        Arrays.fill(gradient, 0.0);
    }

    @Override
    public void clear() {
        Arrays.fill(gradient, 0.0);
    }

    @Override
    public Optional<BufferedImage> render() {
        BufferedImage image = new BufferedImage(grid.width() * GridWorld.SCALE,
                grid.height() * GridWorld.SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.translate(0, image.getHeight());
        graphics.scale(1, -1);

        for(int row = 0; row < grid.height(); ++row)
            for(int column = 0; column < grid.width(); ++column) {
                float hue = 0.65f * (float)(1.0 - occupied(grid.index(row, column)));

                graphics.setPaint(Color.getHSBColor(hue, 1f, 1f));
                graphics.fillRect(column * GridWorld.SCALE,
                        row * GridWorld.SCALE, GridWorld.SCALE, GridWorld.SCALE);
            }

        return Optional.of(image);
    }
}
