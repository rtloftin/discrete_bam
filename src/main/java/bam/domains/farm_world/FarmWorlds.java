package bam.domains.farm_world;

import bam.domains.NavGrid;

import java.util.Arrays;

public class FarmWorlds {

    public static FarmWorld environment(String name, NavGrid grid, Terrain[][] map, Machine[][] machines) {
        return new FarmWorld(name, grid, map, machines);
    }

    /////////////////////////////////////////////
    // Constructors for Predefined Farm Worlds //
    /////////////////////////////////////////////

    public static FarmWorld example() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(12, 8, NavGrid.FOUR);

        // Set terrain
        Terrain[][] map = new Terrain[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(map[row], Terrain.DIRT);

        // Soil
        map[1][1] = Terrain.SOIL;
        map[1][2] = Terrain.SOIL;
        map[2][1] = Terrain.SOIL;
        map[2][2] = Terrain.SOIL;

        // Grass
        map[1][5] = Terrain.GRASS;
        map[1][6] = Terrain.GRASS;
        map[2][5] = Terrain.GRASS;
        map[2][6] = Terrain.GRASS;

        // Crops
        map[1][9]  = Terrain.CROPS;
        map[1][10] = Terrain.CROPS;
        map[2][9]  = Terrain.CROPS;
        map[2][10] = Terrain.CROPS;

        // Set machines
        Machine[][] machines = new Machine[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(machines[row], Machine.NONE);

        machines[6][1] = Machine.PLOW;
        machines[6][3] = Machine.SPRINKLER;
        machines[6][5] = Machine.HARVESTER;

        FarmWorld environment = new FarmWorld("example", grid, map, machines);

        // Build environment
        environment.addGoal("Soil", 1,1,2,2);
        environment.addGoal("Grass", 1,5,2,2);
        environment.addGoal("Crops", 1,9,2,2);

        return environment;
    }
}
