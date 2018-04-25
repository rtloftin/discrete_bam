package bam.domains.farm_world;

import bam.algorithms.DynamicsModel;
import bam.algorithms.optimization.Optimization;
import bam.domains.NavGrid;

import java.util.Arrays;

class NewFarmModel implements DynamicsModel {

    // Navigation grid
    private NavGrid grid;

    // Machine dynamics
    private MachineDynamics dynamics;

    // Terrain map
    private Terrain[][] map;

    // planning depth
    private final int depth;

    // Offset - to avoid singularity
    private final double epsilon = 0.1;

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

    NewFarmModel(NavGrid grid, MachineDynamics dynamics, Terrain[][] map, int depth) {
        this.grid = grid;
        this.dynamics = dynamics;
        this.map = map;
        this.depth = depth;
    }

    @Override
    public void initialize(Optimization optimization) {
        Arrays.fill(parameters, 1.0); // Initially uniform -- do not initialize to zero
        Arrays.fill(gradient, 0.0);

        optimizer = optimization.instance(parameters.length);
    }

    @Override
    public void train(int start, int action, int end, double weight) {
        int next_cell = grid.next(dynamics.cell(start), action);
        Machine current_machine = dynamics.machine(start);
        Terrain current_terrain = map[grid.row(next_cell)][grid.column(next_cell)];

        // Check if this is a transition that depends on the parameters
        if(Terrain.DIRT != current_terrain && Machine.NONE != current_machine) {

            // Compute offset into parameter array
            int offset = Terrain.values().length * current_machine.ordinal();

            // Compute normalization term
            double normal = 0.0;

            for (Terrain terrain : Terrain.values()) {
                double parameter = parameters[offset + terrain.ordinal()];
                normal += parameter * parameter + epsilon;
            }

            normal = 1.0 / normal;

            // Check if the transition was a failure -- wrong machine
            if (dynamics.cell(end) != next_cell) {

                // Compute sub-normalization term -- not counting the current machine
                double sub_normal = 0.0;

                for (Terrain terrain : Terrain.values()) {
                    if(terrain != current_terrain) {
                        double parameter = parameters[offset + terrain.ordinal()];
                        sub_normal += parameter * parameter + epsilon;
                    }
                }

                sub_normal = 1.0 / sub_normal;

                // Compute gradient
                for (Terrain terrain : Terrain.values()) {
                    double scale = 2.0 * weight * parameters[offset + terrain.ordinal()];

                    if(terrain != current_terrain) {
                        gradient[offset + terrain.ordinal()] += scale * (sub_normal - normal);
                    } else {
                        gradient[offset + terrain.ordinal()] -= scale * normal;
                    }
                }

            } else {
                double parameter = parameters[offset + current_terrain.ordinal()];
                double sub_normal = 1.0 / (parameter * parameter + epsilon);

                // Compute gradient
                for (Terrain terrain : Terrain.values()) {
                    double scale = 2.0 * weight * parameters[offset + terrain.ordinal()];

                    if(terrain != current_terrain) {
                        gradient[offset + terrain.ordinal()] -= scale * normal;
                    } else {
                        gradient[offset + terrain.ordinal()] += scale * (sub_normal - normal);
                    }
                }

            }
        }
    }

    @Override
    public void update() {

        // Check if learner was initialized
        if (null == optimizer)
            throw new RuntimeException("Optimization algorithm not initialized");

        // Compute update
        optimizer.update(parameters, gradient);

        // Clip parameters
        for(int i = 0; i < parameters.length; ++i){
            if(parameters[i] > 10.0) {
                parameters[i] = 10.0;
            } else if(parameters[i] < -10.0) {
                parameters[i] = -10.0;
            }
        }

        // Offset parameters -- avoid singularity where the gradient is zero
        for(Machine machine : Machine.values()) {
            int offset = Terrain.values().length * machine.ordinal();
            boolean is_zero = true;

            for(Terrain terrain : Terrain.values())
                is_zero = is_zero && (0.0 == parameters[offset + terrain.ordinal()]);

            if(is_zero) {
                for(Terrain terrain : Terrain.values())
                    parameters[offset + terrain.ordinal()] = 1.0;
            }
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

        if(Terrain.DIRT == map[grid.row(next_cell)][grid.column(next_cell)]) {
            single[0] = dynamics.next(state, action);
            return single;
        }

        if(Machine.NONE == dynamics.machine(state)) {
            single[0] = state;
            return single;
        }

        multiple[0] = dynamics.next(state, action);
        multiple[1] = state;

        return multiple;
    }

    @Override
    public double[] transitions(int state, int action) {
        int next_cell = grid.next(dynamics.cell(state), action);
        Machine current_machine = dynamics.machine(state);
        Terrain current_terrain = map[grid.row(next_cell)][grid.column(next_cell)];

        if(Terrain.DIRT == current_terrain || Machine.NONE == current_machine)
            return determined;

        int offset = Terrain.values().length * current_machine.ordinal();
        double normal = 0.0;

        for(Terrain terrain : Terrain.values()) {
            double parameter = parameters[offset + terrain.ordinal()];
            normal += parameter * parameter + epsilon;
        }

        double parameter = parameters[offset + current_terrain.ordinal()];

        probable[0] = parameter * parameter + epsilon;
        probable[1] = 1.0 - probable[0];

        return probable;
    }
}
