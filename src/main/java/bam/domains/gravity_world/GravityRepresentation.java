package bam.domains.gravity_world;

import bam.algorithms.DynamicsModel;
import bam.algorithms.Representation;
import bam.algorithms.RewardMapping;
import bam.domains.NavGrid;

public class GravityRepresentation implements Representation {

    private final int num_states;
    private final int num_actions;
    private final int depth;

    private final NavGrid grid;

    private final Colors[][] colors;

    private final RewardMapping rewards;

    GravityRepresentation(NavGrid grid, Colors[][] colors, int depth) {
        this.grid = grid;
        this.colors = colors;
        this.depth = depth;

        num_states = 4 * grid.numCells();
        num_actions = 5;

        rewards = new GravityRewards(grid.width(), grid.height());
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
    public RewardMapping rewards() {
        return rewards;
    }

    @Override
    public DynamicsModel newModel() {
        return new GravityModel(grid, colors, depth);
    }
}
