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

        FarmWorld environment = new FarmWorld("tutorial", grid, map, machines);

        // Build environment
        environment.addGoal("Soil", 1,1,2,2);
        environment.addGoal("Grass", 1,5,2,2);
        environment.addGoal("Crops", 1,9,2,2);

        return environment;
    }

    public static FarmWorld twoFields() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(8, 8, NavGrid.FOUR);

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

        machines[6][2] = Machine.PLOW;
        machines[6][5] = Machine.HARVESTER;

        FarmWorld environment = new FarmWorld("two-fields", grid, map, machines);

        // Build environment
        environment.addGoal("Soil", 1,1,2,2);
        environment.addGoal("Crops", 1,5,2,2);

        return environment;
    }

    public static FarmWorld sixFields() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(12, 12, NavGrid.FOUR);

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
        map[6][1] = Terrain.SOIL;
        map[8][1] = Terrain.GRASS;
        map[10][1] = Terrain.CROPS;

        // Set machines
        Machine[][] machines = new Machine[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(machines[row], Machine.NONE);

        machines[10][4] = Machine.PLOW;
        machines[10][6] = Machine.SPRINKLER;
        machines[10][8] = Machine.HARVESTER;

        FarmWorld environment = new FarmWorld("six-fields", grid, map, machines);

        // Build environment
        environment.addGoal("Soil", 1,1,2,2);
        environment.addGoal("Grass", 1,5,2,2);
        environment.addGoal("Crops", 1,9,2,2);

        return environment;
    }
}
