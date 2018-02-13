package bam.domains.grid_world;

import bam.DynamicsModel;
import bam.Representation;
import bam.RewardMapping;
import bam.domains.NavGrid;

class GridRepresentation implements Representation {

    private final NavGrid grid;
    private int depth;
    private final GridRewards rewards;

    GridRepresentation(NavGrid grid, int depth) {
        this.grid = grid;
        this.depth = depth;

        rewards = new GridRewards(grid.width(), grid.height());
    }

    @Override
    public int numStates() {
        return grid.numCells();
    }

    @Override
    public int numActions(int state) {
        return grid.numMoves();
    }

    @Override
    public RewardMapping rewards() {
        return rewards;
    }

    @Override
    public DynamicsModel newModel() {
        return new GridModel(grid, depth);
    }
}
