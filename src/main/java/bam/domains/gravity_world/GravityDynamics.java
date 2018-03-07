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

    GravityDynamics(NavGrid grid, int[][] colors, int[] gravity, int depth) {
        this.num_states = 4 * grid.numCells();
        this.num_actions = 5; // Even if the grid is eight connected, only allow five moves
        this.depth = depth;

        successors = new int[num_states][num_actions][1];

        for(int cell = 0; cell < grid.numCells(); ++cell) {
            int row = grid.row(cell);
            int column = grid.column(cell);

            if(GravityWorld.CLEAR == colors[row][column]) {
                for(int grav = 0; grav < 4; ++grav) {
                    int offset = grav * grid.numCells();
                    int state = offset + cell;

                    for(int action = 0; action < num_actions; ++action) {
                        if(action != GravityWorld.BLOCKED[grav])
                            successors[state][action][0] = offset + grid.next(cell, action);
                        else
                            successors[state][action][0] = state;
                    }
                }
            }
            else {
                int change = gravity[colors[row][column]];
                int offset = change * grid.numCells();

                for(int grav = 0; grav < 4; ++grav) {
                    int state = grav * grid.numCells() + cell;

                    for(int action = 0; action < num_actions; ++action) {
                        if(action != GravityWorld.BLOCKED[change])
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
