package bam.domains.gravity_world;

import bam.domains.NavGrid;

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

    public static void flip() {

    }
}
