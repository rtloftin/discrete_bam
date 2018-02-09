package bam.domains.grid_world;

import bam.Dynamics;
import bam.domains.NavGrid;

class GridDynamics implements Dynamics {

    private NavGrid grid;
    private boolean[][] map;

    GridDynamics(NavGrid grid, boolean[][] map) {
        this.grid = grid;
        this.map = map;
    }

    @Override
    public int numStates() {
        return grid.numStates();
    }

    @Override
    public int numActions(int state) {
        return grid.numActions();
    }

    @Override
    public int depth() {
        return 2 * grid.height() * grid.width();
    }

    @Override
    public int[] successors(int state, int action) {
        int next = grid.next(state, action);

        if(map[grid.row(next)][grid.column(next)])
            return new int[]{ state };

        return new int[]{ next };
    }

    @Override
    public double[] transitions(int state, int action) {
        return new double[] { 1.0 };
    }
}
