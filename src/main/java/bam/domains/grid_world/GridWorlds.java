package bam.domains.grid_world;

import bam.domains.NavGrid;

/**
 * This class contains static methods for generating
 * grid world environments and learning representations.
 *
 * Created by Tyler on 10/9/2017.
 */
public class GridWorlds {

    /**
     * Creates a new grid world with the given occupancy map.
     *
     * @param name the name of the environment
     * @param grid the navigation grid defining this environment
     * @param map the occupancy map of the environment
     * @return the new grid world
     */
    public static GridWorld environment(String name, NavGrid grid, boolean[][] map) {
        return new GridWorld(name, grid, map);
    }

    /////////////////////////////////////////////
    // Constructors for Predefined Grid Worlds //
    /////////////////////////////////////////////

    public static GridWorld empty(int width, int height, int connections) {

        // Initialize grid
        NavGrid grid = new NavGrid(width, height, connections);

        // Initialize map
        boolean[][] map = new boolean[grid.height()][grid.width()];

        for(int i=0; i < map.length; ++i)
            for(int j=0; j < map[i].length; ++j)
                map[i][j] = false;

        // Create environment
        GridWorld environment = new GridWorld("empty-grid", grid, map);

        // Initialize tasks
        environment.addGoal("bottom", 0, 4);
        environment.addGoal("top",8, 4);
        environment.addGoal("left",4, 0);
        environment.addGoal("right",4, 8);

        return environment;
    }

    public static GridWorld centerBlock(int connections) {

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
        GridWorld environment = new GridWorld("center-block", grid, map);

        // Initialize tasks
        // environment.addGoal("bottom", 0, 4);
        environment.addGoal("top",8, 4);
        // environment.addGoal("left",4, 0);
        environment.addGoal("right",4, 8);

        return environment;
    }

    public static GridWorld centerWall(int connections) {

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
        GridWorld environment = new GridWorld("center-wall", grid, map);

        // Initialize tasks
        environment.addGoal("left", 0, 0);
        environment.addGoal("right", 0, 8);

        return environment;
    }

    public static GridWorld twoRooms(int connections) {

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
        GridWorld environment = new GridWorld("two-rooms", grid, map);

        // Initialize tasks
        environment.addGoal("left",0, 14);
        environment.addGoal("right", 0, 0);
        environment.addGoal("inside",2, 5);

        return environment;
    }

    public static GridWorld threeRooms(int connections) {

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
        GridWorld environment = new GridWorld("three-rooms", grid, map);

        // Initialize tasks
        environment.addGoal("left",6, 0);
        environment.addGoal("right", 6, 12);

        return environment;
    }

    public static GridWorld room() {

        // Initialize grid
        NavGrid grid = new NavGrid(11, 11, NavGrid.FOUR);

        // Initialize map
        boolean[][] map = new boolean[grid.height()][grid.width()];

        for(int i=0; i < map.length; ++i)
            for(int j=0; j < map[i].length; ++j)
                map[i][j] = false;

        for(int i=3; i < 8; ++i) {
            map[3][i] = true;
            map[7][i] = true;
        }

        map[4][3] = true;
        map[4][7] = true;
        map[6][3] = true;
        map[6][7] = true;

        // Create environment
        GridWorld environment = new GridWorld("room", grid, map);

        // Initialize tasks
        environment.addGoal("Top",0, 5);
        environment.addGoal("Bottom", 10, 5);

        return environment;
    }

    public static GridWorld doors() {

        // Initialize grid
        NavGrid grid = new NavGrid(13, 9, NavGrid.FOUR);

        // Initialize map
        boolean[][] map = new boolean[grid.height()][grid.width()];

        for(int i=0; i < map.length; ++i)
            for(int j=0; j < map[i].length; ++j)
                map[i][j] = false;

        for(int i=0; i < 13; ++i) {
            map[0][i] = true;
            map[8][i] = true;
        }

        for(int i=1; i < 8; ++i) {
            map[i][0] = true;
            map[i][12] = true;
        }

        for(int i=0; i < 5; ++i) {
            map[4][1 + i] = true;
            map[4][7 + i] = true;
        }

        map[1][4] = true;
        map[1][8] = true;
        map[3][4] = true;
        map[3][8] = true;

        // Create environment
        GridWorld environment = new GridWorld("doors", grid, map);

        // Initialize tasks
        environment.addGoal("Top", 2, 6);
        environment.addGoal("Top Left",2, 2);
        environment.addGoal("Top Right", 2, 10);
        environment.addGoal("Left",6, 2);
        environment.addGoal("Right", 6, 10);

        return environment;
    }
}
