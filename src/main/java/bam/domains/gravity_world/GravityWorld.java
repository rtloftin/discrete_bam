package bam.domains.gravity_world;

import bam.Environment;
import bam.domains.NavGrid;

import java.util.Arrays;

public class GravityWorld {

    // Cell colors -- clear should  be the last color, to give the number of colors, and to index arrays by color
    public static final int BLUE= 0;
    public static final int ORANGE = 1;
    public static final int GREEN = 2;
    public static final int PURPLE = 3;
    public static final int CLEAR = 4;

    // Gravity directions -- each direction's constant matches the action it prevents, to make the logic simpler
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int EAST = 2;
    public static final int WEST = 3;

    // The mapping from gravity directions to blocked actions
    static final int[] BLOCKED = new int[] { NavGrid.DOWN, NavGrid.UP, NavGrid.LEFT, NavGrid.RIGHT };

    // The size of each grid cell in pixels, for visualization.
    static final int SCALE = 40;

    /**
     * Creates a new gravity world environment.
     *
     * @param name the name of the environment
     * @param grid the underlying navigation grid
     * @param colors the colors of the cells
     * @param gravity the gravity associated with each color
     * @return the new environment
     */
    public static GravityEnvironment environment(String name, NavGrid grid, int[][] colors, int[] gravity) {
        return new GravityEnvironment(name, grid, colors, gravity);
    }

    /////////////////////////////////////////////
    // Constructors for Predefined Grid Worlds //
    /////////////////////////////////////////////

    public static Environment flip() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(5, 5, NavGrid.FOUR);

        // Set cell colors
        int[][] colors = new int[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(colors[row], CLEAR);

        colors[0][2] = ORANGE;
        colors[4][2] = GREEN;

        // Set gravity mapping
        int[] gravity = new int[CLEAR];
        gravity[ORANGE] = NORTH;
        gravity[GREEN] = SOUTH;

        // Build environment
        GravityEnvironment environment = new GravityEnvironment("flip", grid, colors, gravity);

        // Define goals
        environment.addGoal("left", 2, 0);
        environment.addGoal("right", 2, 4);

        return environment;
    }

    public static Environment medium_flip() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(10, 10, NavGrid.FOUR);

        // Set cell colors
        int[][] colors = new int[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(colors[row], CLEAR);

        colors[0][1] = ORANGE;
        colors[0][4] = ORANGE;
        colors[0][8] = ORANGE;

        colors[9][1] = GREEN;
        colors[9][4] = GREEN;
        colors[9][8] = GREEN;

        // Set gravity mapping
        int[] gravity = new int[CLEAR];
        gravity[ORANGE] = NORTH;
        gravity[GREEN] = SOUTH;

        // Build environment
        GravityEnvironment environment = new GravityEnvironment("medium-flip", grid, colors, gravity);

        // Define goals
        environment.addGoal("left", 4, 0);
        environment.addGoal("right", 4, 9);

        return environment;
    }

    public static Environment large_flip() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(20, 20, NavGrid.FOUR);

        // Set cell colors
        int[][] colors = new int[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(colors[row], CLEAR);

        colors[0][4] = ORANGE;
        colors[0][10] = ORANGE;
        colors[0][16] = ORANGE;

        colors[19][4] = GREEN;
        colors[19][10] = GREEN;
        colors[19][16] = GREEN;

        // Set gravity mapping
        int[] gravity = new int[CLEAR];
        gravity[ORANGE] = NORTH;
        gravity[GREEN] = SOUTH;

        // Build environment
        GravityEnvironment environment = new GravityEnvironment("large-flip", grid, colors, gravity);

        // Define goals
        environment.addGoal("left", 10, 0);
        environment.addGoal("right", 10, 19);

        return environment;
    }

}
