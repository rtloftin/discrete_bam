package bam.domains.grid_world;

import bam.Dynamics;
import bam.domains.NavGrid;

class GridDynamics implements Dynamics {

    // Number of states and actions, and planning depth
    private final int num_states;
    private final int num_actions;
    private final int depth;

    // Successor states
    private final int[][][] successors;

    // Transition distribution
    private final double[] determined = new double[] { 1.0 };

    GridDynamics(NavGrid grid, boolean[][] map, int depth) {
        this.depth = depth;

        num_states = grid.numCells();
        num_actions = grid.numMoves();

        successors = new int[num_states][num_actions][1];

        for(int state = 0; state < num_states; ++state)
            for(int action = 0; action < num_actions; ++action) {
                int next = grid.next(state, action);

                if(map[grid.row(next)][grid.column(next)])
                    next = state;

                successors[state][action][0] = next;
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
