package bam.domains.farm_world;

public enum Terrain {
    DIRT  (Machine.NONE),
    SOIL  (Machine.PLOW),
    GRASS (Machine.SPRINKLER),
    CROPS (Machine.HARVESTER);

    public final Machine machine;

    Terrain(Machine machine) {
        this.machine = machine;
    }
}
