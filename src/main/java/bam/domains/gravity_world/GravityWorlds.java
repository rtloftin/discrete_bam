package bam.domains.gravity_world;

import bam.domains.NavGrid;

import java.util.Arrays;

public class GravityWorlds {

    /**
     * Creates a new gravity world environment.
     *
     * @param name the name of the environment
     * @param grid the underlying navigation grid
     * @param colors the colors of the cells
     * @param gravity the gravity associated with each color
     * @return the new environment
     */
    public static GravityWorld environment(String name, NavGrid grid, Colors[][] colors, Gravity[] gravity) {
        return new GravityWorld(name, grid, colors, gravity);
    }

    ////////////////////////////////////////////////
    // Constructors for Predefined Gravity Worlds //
    ////////////////////////////////////////////////

    public static GravityWorld tutorial() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(10, 10, NavGrid.FOUR);

        // Set cell colors
        Colors[][] colors = new Colors[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(colors[row], Colors.CLEAR);

        colors[0][1] = Colors.ORANGE;
        colors[0][4] = Colors.ORANGE;
        colors[0][8] = Colors.ORANGE;

        colors[9][1] = Colors.GREEN;
        colors[9][4] = Colors.GREEN;
        colors[9][8] = Colors.GREEN;

        // Set gravity mapping
        Gravity[] gravity = new Gravity[Colors.values().length];
        gravity[Colors.ORANGE.ordinal()] = Gravity.SOUTH;
        gravity[Colors.GREEN.ordinal()] = Gravity.NORTH;

        // Build environment
        GravityWorld environment = new GravityWorld("tutorial", grid, colors, gravity);

        // Define goals
        environment.addGoal("Left", 4, 0);
        environment.addGoal("Right", 4, 9);

        return environment;
    }

    public static GravityWorld flip() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(5, 5, NavGrid.FOUR);

        // Set cell colors
        Colors[][] colors = new Colors[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(colors[row], Colors.CLEAR);

        colors[0][2] = Colors.ORANGE;
        colors[4][2] = Colors.GREEN;

        // Set gravity mapping
        Gravity[] gravity = new Gravity[Colors.values().length];
        gravity[Colors.ORANGE.ordinal()] = Gravity.NORTH;
        gravity[Colors.GREEN.ordinal()] = Gravity.SOUTH;

        // Build environment
        GravityWorld environment = new GravityWorld("flip", grid, colors, gravity);

        // Define goals
        environment.addGoal("Left", 2, 0);
        environment.addGoal("Right", 2, 4);

        return environment;
    }

    public static GravityWorld medium_flip() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(10, 10, NavGrid.FOUR);

        // Set cell colors
        Colors[][] colors = new Colors[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(colors[row], Colors.CLEAR);

        colors[0][1] = Colors.ORANGE;
        colors[0][4] = Colors.ORANGE;
        colors[0][8] = Colors.ORANGE;

        colors[9][1] = Colors.GREEN;
        colors[9][4] = Colors.GREEN;
        colors[9][8] = Colors.GREEN;

        // Set gravity mapping
        Gravity[] gravity = new Gravity[Colors.values().length];
        gravity[Colors.ORANGE.ordinal()] = Gravity.SOUTH;
        gravity[Colors.GREEN.ordinal()] = Gravity.NORTH;

        // Build environment
        GravityWorld environment = new GravityWorld("medium-flip", grid, colors, gravity);

        // Define goals
        environment.addGoal("Left", 4, 0);
        environment.addGoal("Right", 4, 9);

        return environment;
    }

    public static GravityWorld large_flip() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(20, 20, NavGrid.FOUR);

        // Set cell colors
        Colors[][] colors = new Colors[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(colors[row], Colors.CLEAR);

        colors[0][4] = Colors.ORANGE;
        colors[0][10] = Colors.ORANGE;
        colors[0][16] = Colors.ORANGE;

        colors[19][4] = Colors.GREEN;
        colors[19][10] = Colors.GREEN;
        colors[19][16] = Colors.GREEN;

        // Set gravity mapping
        Gravity[] gravity = new Gravity[Colors.values().length];
        gravity[Colors.ORANGE.ordinal()] = Gravity.NORTH;
        gravity[Colors.GREEN.ordinal()] = Gravity.SOUTH;

        // Build environment
        GravityWorld environment = new GravityWorld("large-flip", grid, colors, gravity);

        // Define goals
        environment.addGoal("Left", 10, 0);
        environment.addGoal("Right", 10, 19);

        return environment;
    }

    public static GravityWorld wall() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(5, 5, NavGrid.FOUR);

        // Set cell colors
        Colors[][] colors = new Colors[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(colors[row], Colors.CLEAR);

        colors[1][2] = Colors.GREEN;
        colors[2][2] = Colors.GREEN;
        colors[3][2] = Colors.GREEN;

        // Set gravity mapping
        Gravity[] gravity = new Gravity[Colors.values().length];
        gravity[Colors.GREEN.ordinal()] = Gravity.EAST;

        // Build environment
        GravityWorld environment = new GravityWorld("wall", grid, colors, gravity);

        // Define goals
        environment.addGoal("Top Left", 0, 0);
        environment.addGoal("Bottom Left", 4, 0);

        return environment;
    }

    public static GravityWorld choices() {

        // Initialize navigation grid
        NavGrid grid = new NavGrid(15, 4, NavGrid.FOUR);

        // Set cell colors
        Colors[][] colors = new Colors[grid.height()][grid.width()];

        for(int row = 0; row < grid.height(); ++row)
            Arrays.fill(colors[row], Colors.CLEAR);

        colors[0][4] = Colors.ORANGE;
        colors[0][10] = Colors.ORANGE;

        colors[0][1] = Colors.GREEN;
        colors[0][13] = Colors.PURPLE;

        // Set gravity mapping
        Gravity[] gravity = new Gravity[Colors.values().length];
        gravity[Colors.ORANGE.ordinal()] = Gravity.SOUTH;
        gravity[Colors.GREEN.ordinal()] = Gravity.NORTH;
        gravity[Colors.PURPLE.ordinal()] = Gravity.NORTH;

        // Build environment
        GravityWorld environment = new GravityWorld("choices", grid, colors, gravity);

        // Define goals
        environment.addGoal("Bottom Left", 3, 0);
        environment.addGoal("Bottom Right", 3, 14);

        return environment;
    }

}
