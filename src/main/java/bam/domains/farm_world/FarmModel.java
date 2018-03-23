package bam.domains.farm_world;

import bam.algorithms.DynamicsModel;
import bam.algorithms.optimization.Optimization;
import bam.domains.NavGrid;

import java.util.Arrays;

class FarmModel implements DynamicsModel {

    // Navigation grid
    private NavGrid grid;

    // Machine dynamics
    private MachineDynamics dynamics;

    // Terrain map
    private Terrain[][] map;

    // planning depth
    private final int depth;

    // Parameters and gradient
    private final double[] parameters = new double[Terrain.values().length * Machine.values().length];
    private final double[] gradient = new double[Terrain.values().length * Machine.values().length];

    // Output buffers - to avoid so many heap allocations
    private final double[] determined = new double[] { 1.0 };
    private final double[] probable = new double[2];

    private final int[] single = new int[1];
    private final int[] multiple = new int[2];

    // Parameter optimizer
    private Optimization.Instance optimizer = null;

    FarmModel(NavGrid grid, MachineDynamics dynamics, Terrain[][] map, int depth) {
        this.grid = grid;
        this.dynamics = dynamics;
        this.map = map;
        this.depth = depth;
    }

    @Override
    public void initialize(Optimization optimization) {
        Arrays.fill(parameters, 0.0); // Initially uniform
        Arrays.fill(gradient, 0.0);

        optimizer = optimization.instance(parameters.length);
    }

    @Override
    public void train(int start, int action, int end, double weight) {
        int end_cell = dynamics.cell(end);
        Terrain terrain = map[grid.row(end_cell)][grid.column(end_cell)];

        if(Terrain.DIRT != terrain) { // If we are moving into a clear cell, do nothing
            int offset = Machine.values().length * terrain.ordinal();
            double partition = 0.0;

            for(Machine machine : Machine.values())
                partition += Math.exp(parameters[offset + machine.ordinal()]);

            Machine current_machine = dynamics.machine(start);

            for(Machine machine : Machine.values()) {
                double probability = Math.exp(parameters[offset + machine.ordinal()] = parameters[offset]) / partition;

                if(current_machine != machine)
                    gradient[offset + machine.ordinal()] -= weight * probability;
                else
                    gradient[offset + machine.ordinal()] += weight * (1.0 - probability);
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
    public void clear() { Arrays.fill(gradient, 0.0); }

    @Override
    public int numStates() { return dynamics.numStates(); }

    @Override
    public int numActions(int state) { return dynamics.numActions(); }

    @Override
    public int depth() { return depth; }

    @Override
    public int[] successors(int state, int action) {
        int next_cell = grid.next(dynamics.cell(state), action);
        Terrain terrain = map[grid.row(next_cell)][grid.column(next_cell)];

        if(Terrain.DIRT == terrain) {
            single[0] = dynamics.next(state, action);
            return single;
        }

        multiple[0] = dynamics.next(state, action);
        multiple[1] = state;

        return multiple;
    }

    @Override
    public double[] transitions(int state, int action) {
        int next_cell = grid.next(dynamics.cell(state), action);
        Terrain terrain = map[grid.row(next_cell)][grid.column(next_cell)];

        if(Terrain.DIRT == terrain)
            return determined;

        int offset = Machine.values().length * terrain.ordinal();
        double partition = 0.0;

        for(Machine machine : Machine.values())
            partition += Math.exp(parameters[offset + machine.ordinal()] - parameters[offset]);

        Machine machine = dynamics.machine(state);

        probable[0] = Math.exp(parameters[offset + machine.ordinal()] = parameters[offset]) / partition;
        probable[1] = 1.0 - probable[0];

        return probable;
    }
}
