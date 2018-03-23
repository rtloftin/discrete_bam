package bam.domains.gravity_world;

import bam.domains.NavGrid;

public enum Gravity {
    NORTH (NavGrid.DOWN),
    SOUTH (NavGrid.UP),
    EAST (NavGrid.LEFT),
    WEST (NavGrid.RIGHT);

    public final int blocks;

    Gravity(int blocks) { this.blocks = blocks; }
}
