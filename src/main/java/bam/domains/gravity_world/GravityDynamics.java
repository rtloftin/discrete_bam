package bam.domains.gravity_world;

import bam.algorithms.Dynamics;
import bam.domains.NavGrid;

public class GravityDynamics implements Dynamics {

    // Number of states and actions, and planning depth
    private final int num_states;
    private final int num_actions;
    private final int depth;

    // Successor states
    private final int[][][] successors;

    // Transition distribution
    private final double[] determined = new double[] { 1.0 };

    GravityDynamics(NavGrid grid, Colors[][] colors, Gravity[] mapping, int depth) {
        this.num_states = Gravity.values().length * grid.numCells();
        this.num_actions = 5; // Even if the grid is eight connected, only allow five moves
        this.depth = depth;

        successors = new int[num_states][num_actions][1];

        for(int cell = 0; cell < grid.numCells(); ++cell) {
            int row = grid.row(cell);
            int column = grid.column(cell);

            if(Colors.CLEAR == colors[row][column]) {
                for(Gravity gravity : Gravity.values()) {
                    int offset = gravity.ordinal() * grid.numCells();
                    int state = offset + cell;

                    for(int action = 0; action < num_actions; ++action) {
                        if(action != gravity.blocks)
                            successors[state][action][0] = offset + grid.next(cell, action);
                        else
                            successors[state][action][0] = state;
                    }
                }
            }
            else {
                Gravity new_gravity = mapping[colors[row][column].ordinal()];
                int offset = new_gravity.ordinal() * grid.numCells();

                for(Gravity gravity : Gravity.values()) {
                    int state = (gravity.ordinal() * grid.numCells()) + cell;

                    for(int action = 0; action < num_actions; ++action) {
                        if(action != new_gravity.blocks)
                            successors[state][action][0] = offset + grid.next(cell, action);
                        else
                            successors[state][action][0] = offset + cell;
                    }
                }
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
