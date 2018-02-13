package bam.domains.gravity_world;

import bam.DynamicsModel;
import bam.algorithms.optimization.Optimization;
import bam.domains.NavGrid;

import java.util.Arrays;

/**
 * A learned model of the mapping from colors
 * to gravity changes.
 *
 * Warning: this class is not thread safe.
 *
 */
public class GravityModel implements DynamicsModel {

    // Underlying navigation grid
    private final NavGrid grid;

    // Cell colors
    private final int[][] colors;

    // Number of states and actions, and planning depth
    private final int num_states;
    private final int num_actions;
    private final int depth;

    // Parameters and gradient
    private final double[] parameters = new double[4 * GravityWorld.CLEAR]; // Maybe three, reduce redundancy
    private final double[] gradient = new double[4 * GravityWorld.CLEAR];

    // Output buffers - to avoid so many heap allocations
    private final double[] determined = new double[] { 1.0 };
    private final double[] probable = new double[GravityWorld.CLEAR];

    private final int[] single = new int[1];
    private final int[] multiple = new int[GravityWorld.CLEAR];

    // Parameter optimizer
    private Optimization.Instance optimizer = null;

    GravityModel(NavGrid grid, int[][] colors, int depth) {
        this.grid = grid;
        this.colors = colors;
        this.depth = depth;

        num_states = 4 * grid.numCells();
        num_actions = 5;
    }

    @Override
    public void initialize(Optimization optimization) {
        Arrays.fill(parameters, 0.25); // Initially uniform
        Arrays.fill(gradient, 0.0);

        optimizer = optimization.instance(parameters.length);
    }

    @Override
    public void train(int start, int action, int end, double weight) {
        int cell = start % grid.numCells();
        int row = grid.row(cell);
        int column = grid.column(cell);

        if(GravityWorld.CLEAR == colors[row][column]) {
            int gravity = end / grid.numCells();
            int offset = 4 * colors[row][column];

            double partition = 0.0;

            for(int grav = 0; grav < 4; ++grav)
                partition += Math.exp(parameters[offset + grav]);

            for(int grav = 0; grav < 4; ++grav) {
                if(grav != gravity)
                    gradient[offset + grav] += weight * (1.0 - Math.exp(parameters[offset + grav]) / partition);
                else
                    gradient[offset + grav] -= weight * Math.exp(parameters[offset + grav]) / partition;
            }
        }
    }

    @Override
    public void update() {

        // Check if learner was initialized
        if (null == optimizer)
            throw new RuntimeException("Optimization algorithm not initialized");

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
    public int numStates() {
        return num_states;
    }

    @Override
    public int numActions(int state) {
        return num_actions;
    }

    @Override
    public int depth() {
        return depth;
    }

    @Override
    public int[] successors(int state, int action) {
        int cell = state % grid.numCells();
        int row = grid.row(cell);
        int column = grid.column(cell);

        if(GravityWorld.CLEAR == colors[row][column]) {
            if(action == GravityWorld.BLOCKED[state / grid.numCells()]) // Action fails
                single[0] = state;
            else
                single[0] = state - cell + grid.next(cell, action);

            return single;
        }

        for(int gravity = 0; gravity < 4; ++gravity) {
            if(action == GravityWorld.BLOCKED[gravity]) // Action fails
                multiple[gravity] = (gravity * grid.numCells()) + cell;
            else
                multiple[gravity] = (gravity * grid.numCells()) + grid.next(cell, action);
        }

        return multiple;
    }

    @Override
    public double[] transitions(int state, int action) {
        int cell = state % grid.numCells();
        int row = grid.row(cell);
        int column = grid.column(cell);

        if(GravityWorld.CLEAR == colors[row][column])
            return determined;

        int offset = colors[row][column];
        double partition = 0.0;

        for(int gravity = 0; gravity < 4; ++gravity) {
            probable[gravity] = Math.exp(parameters[offset + gravity] - parameters[offset]);
            partition += probable[gravity];
        }

        for(int gravity = 0; gravity < 4; ++gravity)
            probable[gravity] /= partition;

        return probable;
    }
}
