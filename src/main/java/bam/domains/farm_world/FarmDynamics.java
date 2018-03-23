package bam.domains.farm_world;

import bam.algorithms.Dynamics;
import bam.domains.NavGrid;

class FarmDynamics implements Dynamics {

    // Number of states and actions, and planning depth
    private final int num_states;
    private final int num_actions;
    private final int depth;

    // Successor states
    private final int[][][] successors;

    // Transition distribution
    private final double[] determined = new double[] { 1.0 };

    FarmDynamics(NavGrid grid, Terrain[][] map, Machine[][] machines, int depth) {
        MachineDynamics dynamics = new MachineDynamics(grid, machines);

        this.num_states = dynamics.numStates();
        this.num_actions = dynamics.numActions();
        this.depth = depth;

        this.successors = new int[num_states][num_actions][1];

        for(int state = 0; state < num_states; ++state) {
            int cell = dynamics.cell(state);
            Machine machine = dynamics.machine(state);

            for (int action = 0; action < num_actions; ++action) {
                int next_cell = grid.next(cell, action);
                Terrain terrain = map[ grid.row(next_cell)][grid.column(next_cell)];

                if(Terrain.DIRT == terrain || terrain.machine == machine)
                    successors[state][action][0] = dynamics.next(state, action);
                else
                    successors[state][action][0] = state;
            }
        }
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
        return successors[state][action];
    }

    @Override
    public double[] transitions(int state, int action) {
        return determined;
    }
}
