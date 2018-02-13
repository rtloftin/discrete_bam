package bam.domains;

/**
 * This class represents a 2D navigation grid,
 * either 4 or 8 connected.
 *
 * Created by Tyler on 5/24/2017.
 */
public class NavGrid {

    // Grid connection types
    public static final int FOUR = 0;
    public static final int EIGHT = 1;

    // Action types
    public static final int STAY = 0;
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int UP_LEFT = 5;
    public static final int UP_RIGHT = 6;
    public static final int DOWN_LEFT = 7;
    public static final int DOWN_RIGHT = 8;

    // The width and height of the grid
    private int width;
    private int height;

    // The number of states and actions
    private int num_cells;
    private int num_moves;

    // The successors of each state
    private int[][] successors;

    public NavGrid(int width, int height, int connect) {
        this.width = width;
        this.height = height;

        num_cells = width * height;
        num_moves = (EIGHT == connect) ? 9 : 5;

        successors = new int[num_cells][num_moves];

        // Common actions

        for(int row = 0; row < height; ++row)
            for(int column = 0; column < width; ++column) {
                int state = index(row, column);
                successors[state][STAY] = state;
            }

        for(int row = 0; row < height; ++row) // UP
            for(int column = 0; column < width; ++column) {
                int state = index(row, column);

                if(height - 1 == row)
                    successors[state][UP] = state;
                else
                    successors[state][UP] = index(row + 1, column);
            }

        for(int row = 0; row < height; ++row) // DOWN
            for(int column = 0; column < width; ++column) {
                int state = index(row, column);

                if(0 == row)
                    successors[state][DOWN] = state;
                else
                    successors[state][DOWN] = index(row - 1, column);
            }

        for(int row = 0; row < height; ++row) // LEFT
            for(int column = 0; column < width; ++column) {
                int state = index(row, column);

                if(0 == column)
                    successors[state][LEFT] = state;
                else
                    successors[state][LEFT] = index(row, column - 1);
            }

        for(int row = 0; row < height; ++row) // RIGHT
            for(int column = 0; column < width; ++column) {
                int state = index(row, column);

                if(width - 1 == column)
                    successors[state][RIGHT] = state;
                else
                    successors[state][RIGHT] = index(row, column + 1);
            }

        // Eight connected actions
        if(EIGHT == connect) {
            for(int row = 0; row < height; ++row) // UP LEFT
                for(int column = 0; column < width; ++column) {
                    int state = index(row, column);

                    if(height - 1 == row || 0 == column)
                        successors[state][UP_LEFT] = state;
                    else
                        successors[state][UP_LEFT] = index(row + 1, column - 1);
                }

            for(int row = 0; row < height; ++row) // UP RIGHT
                for(int column = 0; column < width; ++column) {
                    int state = index(row, column);

                    if(height - 1 == row || width - 1 == column)
                        successors[state][UP_RIGHT] = state;
                    else
                        successors[state][UP_RIGHT] = index(row + 1, column + 1);
                }

            for(int row = 0; row < height; ++row) // DOWN LEFT
                for(int column = 0; column < width; ++column) {
                    int state = index(row, column);

                    if(0 == row || 0 == column)
                        successors[state][DOWN_LEFT] = state;
                    else
                        successors[state][DOWN_LEFT] = index(row - 1, column - 1);
                }

            for(int row = 0; row < height; ++row) // DOWN RIGHT
                for(int column = 0; column < width; ++column) {
                    int state = index(row, column);

                    if(0 == row || width - 1 == column)
                        successors[state][DOWN_RIGHT] = state;
                    else
                        successors[state][DOWN_RIGHT] = index(row - 1, column + 1);
                }
        }
    }

    /**
     * Returns the height of the grid.
     *
     * @return the height of the grid
     */
    public int height() {
        return height;
    }

    /**
     * Returns the width of the grid.
     *
     * @return the width of the grid
     */
    public int width() {
        return width;
    }

    /**
     * Returns the total number of discrete states.
     *
     * @return the number of states
     */
    public int numCells() {
        return num_cells;
    }

    /**
     * Returns the number of discrete actions
     *
     * @return the number of actions
     */
    public int numMoves() { return num_moves; }

    /**
     * Returns the index of the state to which the
     * given action will transition from the given state.
     *
     * @param state the current state
     * @param action the current action
     * @return the resulting state
     */
    public int next(int state, int action) {
        return successors[state][action];
    }

    /**
     * Returns the row for a given state index.
     *
     * @param index the state index
     * @return the row
     */
    public int row(int index) {
        return index % height;
    }

    /**
     * Returns the column for a given state index.
     *
     * @param index the state index
     * @return the column
     */
    public int column(int index) {
        return index / height;
    }

    /**
     * Returns the state index for a row and column.
     *
     * @param row the row
     * @param column the column
     * @return the state index
     */
    public int index(int row, int column) {
        return row + (column * height);
    }
}
