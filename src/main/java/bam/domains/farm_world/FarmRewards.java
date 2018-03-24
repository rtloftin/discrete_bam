package bam.domains.farm_world;

import bam.algorithms.RewardMapping;
import bam.domains.NavGrid;

class FarmRewards implements RewardMapping {

    private NavGrid grid;
    private Terrain[][] map;

    FarmRewards(NavGrid grid, Terrain[][] map) {
        this.grid = grid;
        this.map = map;
    }

    @Override
    public double reward(int state, double[] intent) {
        int cell = state / Machine.values().length;

        if(Terrain.DIRT == map[grid.row(cell)][grid.column(cell)])
            return 0.0;

        return intent[state / Machine.values().length];
    }

    @Override
    public void gradient(int state, double[] intent, double weight, double[] gradient) {
        int cell = state / Machine.values().length;

        if(Terrain.DIRT != map[grid.row(cell)][grid.column(cell)])
            gradient[state / Machine.values().length] += weight;
    }

    @Override
    public int intentSize() {
        return grid.numCells();
    }
}
