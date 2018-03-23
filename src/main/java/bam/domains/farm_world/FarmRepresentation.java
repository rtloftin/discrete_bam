package bam.domains.farm_world;

import bam.algorithms.DynamicsModel;
import bam.algorithms.Representation;
import bam.algorithms.RewardMapping;
import bam.domains.NavGrid;

public class FarmRepresentation implements Representation {

    private final int num_states;
    private final int num_actions;
    private final int depth;

    private final NavGrid grid;
    private Terrain[][] map;

    private final MachineDynamics dynamics;
    private final RewardMapping rewards;

    FarmRepresentation(NavGrid grid, Terrain[][] map, Machine[][] machines, int depth) {
        this.grid = grid;
        this.map = map;

        this.dynamics = new MachineDynamics(grid, machines);
        this.rewards = new FarmRewards(grid, map);

        this.num_states = dynamics.numStates();
        this.num_actions = dynamics.numActions();
        this.depth = depth;
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
        return new FarmModel(grid, dynamics, map, depth);
    }
}
