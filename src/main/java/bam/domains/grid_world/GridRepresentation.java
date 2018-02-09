package bam.domains.grid_world;

import bam.DynamicsModel;
import bam.Representation;
import bam.RewardMapping;
import bam.domains.NavGrid;

class GridRepresentation implements Representation {

    private final NavGrid grid;
    private final GridRewards rewards;

    GridRepresentation(NavGrid grid) {
        this.grid = grid;

        rewards = new GridRewards(grid.width(), grid.height());
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
    public RewardMapping rewards() {
        return rewards;
    }

    @Override
    public DynamicsModel newModel() {
        return new OccupancyModel(grid);
    }
}
