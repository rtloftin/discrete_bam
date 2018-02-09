package bam.domains.grid_world;

import bam.domains.NavGrid;

/**
 * This class contains static methods for generating
 * grid world environments and learning representations.
 *
 * Created by Tyler on 10/9/2017.
 */
public class GridWorld {

    /**
     * The size of each grid cell in pixels, for visualization.
     */
    static final int SCALE = 40;

    /**
     * Creates a new grid world with the given occupancy map.
     *
     * @param name the name of the environment
     * @param grid the navigation grid defining this environment
     * @param map the occupancy map of the environment
     * @return the new grid world
     */
    public static GridEnvironment environment(String name, NavGrid grid, boolean[][] map) {
        return new GridEnvironment(name, grid, map);
    }

    /////////////////////////////////////////////
    // Constructors for Predefined Grid Worlds //
    /////////////////////////////////////////////

    public static GridEnvironment empty(int width, int height, int connections) {

        // Initialize grid
        NavGrid grid = new NavGrid(width, height, connections);

        // Initialize map
        boolean[][] map = new boolean[grid.height()][grid.width()];

        for(int i=0; i < map.length; ++i)
            for(int j=0; j < map[i].length; ++j)
                map[i][j] = false;

        // Create environment
        GridEnvironment environment = new GridEnvironment("empty-grid", grid, map);

        // Initialize tasks
        environment.addGoal("bottom", 0, 4);
        environment.addGoal("top",8, 4);
        environment.addGoal("left",4, 0);
        environment.addGoal("right",4, 8);

        return environment;
    }

    public static GridEnvironment centerBlock(int connections) {

        // Initialize grid
        NavGrid grid = new NavGrid(9, 9, connections);

        // Initialize map
        boolean[][] map = new boolean[9][9];

        for(int i=0; i < map.length; ++i)
            for(int j=0; j < map[i].length; ++j)
                map[i][j] = false;

        for(int i=3; i < 6; ++i)
            for(int j=3; j < 6; ++j)
                map[i][j] = true;

        // Create gridworld
        GridEnvironment environment = new GridEnvironment("center-block", grid, map);

        // Initialize tasks
        // environment.addGoal("bottom", 0, 4);
        environment.addGoal("top",8, 4);
        // environment.addGoal("left",4, 0);
        environment.addGoal("right",4, 8);

        return environment;
    }

    public static GridEnvironment centerWall(int connections) {

        // Initialize grid
        NavGrid grid = new NavGrid(9, 9, connections);

        // Initialize map
        boolean[][] map = new boolean[9][9];

        for(int i=0; i < map.length; ++i)
            for(int j=0; j < map[i].length; ++j)
                map[i][j] = false;

        for(int i=0; i < 6; ++i)
            for(int j=3; j < 6; ++j)
                map[i][j] = true;

        // Create environment
        GridEnvironment environment = new GridEnvironment("center-wall", grid, map);

        // Initialize tasks
        environment.addGoal("left", 0, 0);
        environment.addGoal("right", 0, 8);

        return environment;
    }

    public static GridEnvironment twoRooms(int connections) {

        // Initialize grid
        NavGrid grid = new NavGrid(15, 8, connections);

        // Initialize map
        boolean[][] map = new boolean[grid.height()][grid.width()];

        for(int i=0; i < map.length; ++i)
            for(int j=0; j < map[i].length; ++j)
                map[i][j] = false;

        for(int i=3; i < 12; ++i) {
            map[0][i] = true;
            map[4][i] = true;
        }

        map[1][7] = true;
        map[2][7] = true;
        map[3][7] = true;

        map[1][3] = true;
        map[3][3] = true;
        map[1][11] = true;
        map[3][11] = true;

        // Create environment
        GridEnvironment environment = new GridEnvironment("two-rooms", grid, map);

        // Initialize tasks
        environment.addGoal("left",0, 14);
        environment.addGoal("right", 0, 0);
        environment.addGoal("inside",2, 5);

        return environment;
    }

    public static GridEnvironment threeRooms(int connections) {

        // Initialize grid
        NavGrid grid = new NavGrid(13, 13, connections);

        // Initialize map
        boolean[][] map = new boolean[grid.height()][grid.width()];

        for(int i=0; i < map.length; ++i)
            for(int j=0; j < map[i].length; ++j)
                map[i][j] = false;

        for(int i=2; i < 11; ++i) {
            map[2][i] = true;
            map[6][i] = true;
            map[10][i] = true;
        }

        for(int i=2; i < 11; ++i) {
            map[i][2] = true;
            map[i][6] = true;
            map[i][10] = true;
        }

        map[3][6] = false;
        map[4][6] = false;
        map[5][6] = false;

        map[4][2] = false;
        map[8][2] = false;
        map[4][10] = false;
        map[8][10] = false;

        // Create environment
        GridEnvironment environment = new GridEnvironment("three-rooms", grid, map);

        // Initialize tasks
        environment.addGoal("left",6, 0);
        environment.addGoal("right", 6, 12);

        return environment;
    }
}
