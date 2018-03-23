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
        int cell = state % grid.numCells();
        Terrain terrain = map[grid.row(cell)][grid.column(cell)];

        if(Terrain.DIRT == terrain)
            return 0.0;

        return intent[terrain.ordinal()];
    }

    @Override
    public void gradient(int state, double[] intent, double weight, double[] gradient) {
        int cell = state % grid.numCells();
        Terrain terrain = map[grid.row(cell)][grid.column(cell)];

        if(Terrain.DIRT != terrain)
            gradient[terrain.ordinal()] += weight;
    }

    @Override
    public int intentSize() {
        return Terrain.values().length;
    }
}
