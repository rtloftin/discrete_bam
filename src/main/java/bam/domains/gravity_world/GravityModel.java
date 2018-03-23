package bam.domains.gravity_world;

import bam.algorithms.DynamicsModel;
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
    private final Colors[][] colors;

    // Number of states and actions, and planning depth
    private final int num_states;
    private final int num_actions;
    private final int depth;

    // Parameters and gradient
    private final double[] parameters = new double[Gravity.values().length * Colors.values().length];
    private final double[] gradient = new double[Gravity.values().length * Colors.values().length];

    // Output buffers - to avoid so many heap allocations
    private final double[] determined = new double[] { 1.0 };
    private final double[] probable = new double[Gravity.values().length];

    private final int[] single = new int[1];
    private final int[] multiple = new int[Gravity.values().length];

    // Parameter optimizer
    private Optimization.Instance optimizer = null;

    GravityModel(NavGrid grid, Colors[][] colors, int depth) {
        this.grid = grid;
        this.colors = colors;
        this.depth = depth;

        num_states = Gravity.values().length * grid.numCells();
        num_actions = 5;
    }

    @Override
    public void initialize(Optimization optimization) {
        Arrays.fill(parameters, 0.0); // Initially uniform
        Arrays.fill(gradient, 0.0);

        optimizer = optimization.instance(parameters.length);
    }

    @Override
    public void train(int start, int action, int end, double weight) {

        // if(!Double.isFinite(weight))
           // throw new RuntimeException("Gravity Model: training weight was invalid");

        int cell = start % grid.numCells();
        int row = grid.row(cell);
        int column = grid.column(cell);

        if(Colors.CLEAR != colors[row][column]) { // If the cell is clear then there is nothing to learn
            int gravity = end / grid.numCells();
            int offset = Gravity.values().length * colors[row][column].ordinal();

            double partition = 0.0;

            for(int grav = 0; grav < Gravity.values().length; ++grav)
                partition += Math.exp(parameters[offset + grav] - parameters[offset]);

            // if(!Double.isFinite(partition))
               // throw new RuntimeException("Gravity Model Training: encountered NaN");

            for(int grav = 0; grav < Gravity.values().length; ++grav) {
                double probability = Math.exp(parameters[offset + grav] - parameters[offset]) / partition;

                if(grav != gravity)
                    gradient[offset + grav] -= weight * probability;
                else
                    gradient[offset + grav] += weight * (1.0 - probability);
            }
        }
    }

    @Override
    public void update() {

        // Check if learner was initialized
        if (null == optimizer)
            throw new RuntimeException("Optimization algorithm not initialized");

        // Incorporate regularization term
        for(int i = 0; i < parameters.length; ++i)
            gradient[i] -= parameters[i];

        // Perform update
        optimizer.update(parameters, gradient);

        // Clip parameters
        for(int i = 0; i < parameters.length; ++i){
            if(parameters[i] > 50.0)
                parameters[i] = 50.0;
            else if(parameters[i] < -50.0)
                parameters[i] = -50.0;
        }

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

        if(Colors.CLEAR == colors[row][column]) {
            if(action == Gravity.values()[state / grid.numCells()].blocks) // Action fails
                single[0] = state;
            else
                single[0] = state - cell + grid.next(cell, action);

            return single;
        }

        for(Gravity gravity : Gravity.values()) {
            if(action == gravity.blocks) // Action fails
                multiple[gravity.ordinal()] = (gravity.ordinal() * grid.numCells()) + cell;
            else
                multiple[gravity.ordinal()] = (gravity.ordinal() * grid.numCells()) + grid.next(cell, action);
        }

        return multiple;
    }

    @Override
    public double[] transitions(int state, int action) {
        int cell = state % grid.numCells();
        int row = grid.row(cell);
        int column = grid.column(cell);

        if(Colors.CLEAR == colors[row][column])
            return determined;

        int offset = Gravity.values().length * colors[row][column].ordinal();
        double partition = 0.0;

        for(Gravity gravity : Gravity.values()) {
            probable[gravity.ordinal()] = Math.exp(parameters[offset + gravity.ordinal()] - parameters[offset]);
            partition += probable[gravity.ordinal()];
        }

        // if(!Double.isFinite(partition))
           // throw new RuntimeException("Gravity Model Transition: encountered NaN");

        for(Gravity gravity : Gravity.values())
            probable[gravity.ordinal()] /= partition;

        return probable;
    }
}
