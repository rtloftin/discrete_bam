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

    public static FarmWorld tutorial() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(12, 11, NavGrid.FOUR);

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

        // Decoy soil
        map[5][9] = Terrain.SOIL;
        map[5][10] = Terrain.SOIL;
        map[6][9] = Terrain.SOIL;
        map[6][10] = Terrain.SOIL;

        // Decoy grass
        map[5][1] = Terrain.GRASS;
        map[5][2] = Terrain.GRASS;
        map[6][1] = Terrain.GRASS;
        map[6][2] = Terrain.GRASS;

        // Set machines
        Machine[][] machines = new Machine[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(machines[row], Machine.NONE);

        machines[9][1] = Machine.PLOW;
        machines[9][3] = Machine.SPRINKLER;
        machines[9][5] = Machine.HARVESTER;

        FarmWorld environment = new FarmWorld("tutorial", grid, map, machines);

        // Build environment
        environment.addGoal("Soil", 1,1,2,2);
        environment.addGoal("Grass", 1,5,2,2);
        environment.addGoal("Crops", 1,9,2,2);

        return environment;
    }

    public static FarmWorld twoFields() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(8, 11, NavGrid.FOUR);

        // Set terrain
        Terrain[][] map = new Terrain[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(map[row], Terrain.DIRT);

        // Soil
        map[1][1] = Terrain.SOIL;
        map[1][2] = Terrain.SOIL;
        map[2][1] = Terrain.SOIL;
        map[2][2] = Terrain.SOIL;

        // Crops
        map[1][5]  = Terrain.CROPS;
        map[1][6] = Terrain.CROPS;
        map[2][5]  = Terrain.CROPS;
        map[2][6] = Terrain.CROPS;

        // Set machines
        Machine[][] machines = new Machine[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(machines[row], Machine.NONE);

        machines[4][2] = Machine.HARVESTER;
        machines[4][5] = Machine.SPRINKLER;
        machines[9][3] = Machine.PLOW;


        FarmWorld environment = new FarmWorld("two-fields", grid, map, machines);

        // Build environment
        environment.addGoal("Soil", 1,1,2,2);
        environment.addGoal("Crops", 1,5,2,2);

        return environment;
    }

    public static FarmWorld threeFields() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(8, 11, NavGrid.FOUR);

        // Set terrain
        Terrain[][] map = new Terrain[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(map[row], Terrain.DIRT);

        // Soil
        map[1][1] = Terrain.SOIL;
        map[1][2] = Terrain.SOIL;
        map[2][1] = Terrain.SOIL;
        map[2][2] = Terrain.SOIL;

        map[8][1] = Terrain.SOIL;
        map[8][2] = Terrain.SOIL;
        map[9][1] = Terrain.SOIL;
        map[9][2] = Terrain.SOIL;

        // Grass
        map[1][5]  = Terrain.GRASS;
        map[1][6] = Terrain.GRASS;
        map[2][5]  = Terrain.GRASS;
        map[2][6] = Terrain.GRASS;

        // Set machines
        Machine[][] machines = new Machine[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(machines[row], Machine.NONE);

        machines[4][2] = Machine.SPRINKLER;
        machines[9][5] = Machine.PLOW;

        FarmWorld environment = new FarmWorld("three-fields", grid, map, machines);

        // Build environment
        environment.addGoal("Soil", 1,1,2,2);

        return environment;
    }

    public static FarmWorld sixFields() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(12, 11, NavGrid.FOUR);

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

        // Test fields
        map[5][1] = Terrain.SOIL;
        map[7][1] = Terrain.GRASS;
        map[9][1] = Terrain.CROPS;

        map[5][10] = Terrain.CROPS;
        map[7][10] = Terrain.GRASS;
        map[9][10] = Terrain.SOIL;

        // Set machines
        Machine[][] machines = new Machine[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(machines[row], Machine.NONE);

        machines[9][4] = Machine.PLOW;
        machines[9][6] = Machine.SPRINKLER;
        machines[9][8] = Machine.HARVESTER;

        FarmWorld environment = new FarmWorld("six-fields", grid, map, machines);

        // Build environment
        environment.addGoal("Soil", 1,1,2,2);
        environment.addGoal("Grass", 1,5,2,2);
        environment.addGoal("Crops", 1,9,2,2);

        return environment;
    }
}
