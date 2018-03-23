package bam.domains.farm_world;

import bam.domains.NavGrid;

class MachineDynamics {

    // Number of states and actions, and planning depth
    private final int num_states;
    private final int num_actions;

    // Successor states
    private final int[][] next;

    MachineDynamics(NavGrid grid, Machine[][] machines) {
        this.num_states = Machine.values().length * grid.numCells();
        this.num_actions = grid.numMoves();

        this.next = new int[num_states][num_actions];

        for(int row = 0; row < grid.height(); ++row)
            for(int column = 0; column < grid.width(); ++column) {
                int cell = grid.index(row, column);

                for(Machine machine : Machine.values()) {
                    int state = (cell * Machine.values().length) + machine.ordinal();

                    for(int action = 0; action < num_actions; ++action) {
                        int next_cell = grid.next(cell, action);
                        Machine next_machine = machines[grid.row(next_cell)][grid.column(next_cell)];

                        if(Machine.NONE != next_machine) {
                            next[state][action] = (next_cell * Machine.values().length) + next_machine.ordinal();
                        } else {
                            next[state][action] = (next_cell * Machine.values().length) + machine.ordinal();
                        }
                    }
                }
            }
    }

    int numStates() { return num_states; }

    int numActions() { return num_actions; }

    Machine machine(int state) { return Machine.values()[state % Machine.values().length]; }

    int cell(int state) { return state / Machine.values().length; }

    int next(int state, int action) { return next[state][action]; }
}
