package bam.domains.farm_world;

import bam.algorithms.Dynamics;
import bam.algorithms.Representation;
import bam.domains.Environment;
import bam.domains.NavGrid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class FarmWorld implements Environment {

    public class Task implements bam.domains.Task {

        private final int row, column, width, height;
        private final String name;
        private final double[] rewards;

        private Task(String name, int row, int column, int width, int height) {
            this.name = name;
            this.row = row;
            this.column = column;
            this.width = width;
            this.height = height;

            // Initialize reward function
            rewards = new double[grid.numCells()];
            Arrays.fill(rewards, 0.0);

            // Set goal reward
            for(int r = 0; r < height; ++r)
                for(int c = 0; c < width; ++c)
                    rewards[grid.index(row + r, column + c)] = 1.0;
        }

        private Task(JSONObject config) throws JSONException {
            this(config.getString("name"), config.getInt("row"), config.getInt("column"),
                    config.getInt("width"), config.getInt("height"));
        }

        public int row() { return row; }

        public int column() { return column; }

        public int width() { return width; }

        public int height() { return height; }

        @Override
        public int initial(Random random) {
            int row, column;

            do {
                row = random.nextInt(grid.height());
                column = random.nextInt(grid.width());
            } while(Terrain.DIRT != map[row][column] || Machine.NONE != machines[row][column]);

            return (grid.index(row, column) * Machine.values().length) + Machine.NONE.ordinal();
        }

        @Override
        public double reward(int state) { return rewards[state / Machine.values().length ]; }

        @Override
        public String name() { return name; }

        @Override
        public JSONObject serialize() throws JSONException {
            return new JSONObject()
                    .put("name", name)
                    .put("row", row)
                    .put("column", column)
                    .put("width", width)
                    .put("height", height);
        }
    }

    private final String name;

    private Terrain[][] map;
    private Machine[][] machines;
    private final NavGrid grid;
    private final int depth;

    private final List<Task> tasks;

    private FarmDynamics dynamics;
    private FarmRepresentation representation;

    FarmWorld(String name, NavGrid grid, Terrain[][] map, Machine[][] machines) {
        this.name = name;
        this.map = map;
        this.machines = machines;
        this.grid = grid;

        depth = 4 * (grid.width() + grid.height());

        tasks = new LinkedList<>();

        dynamics = new FarmDynamics(grid, map, machines, depth);
        representation = new FarmRepresentation(grid, map, machines, depth);
    }

    private void addGoal(JSONObject config) throws JSONException {
        tasks.add(new Task(config));
    }

    public void addGoal(String name, int row, int column, int width, int height) {
        tasks.add(new Task(name, row, column, width, height));
    }

    public int width() {
        return grid.width();
    }

    public int height() {
        return grid.height();
    }

    public int index(int row, int column, Machine machine) {
        return (grid.index(row, column) * Machine.values().length) + Machine.NONE.ordinal();
    }

    public int row(int state) { return grid.row(state / Machine.values().length); }

    public int column(int state) {return grid.column(state / Machine.values().length); }

    public Machine machine(int state) { return Machine.values()[state % Machine.values().length]; }

    public Terrain[][] map() { return map; }

    public Machine[][] machines() { return machines; }

    @Override
    public Dynamics dynamics() {
        return dynamics;
    }

    @Override
    public List<? extends Task> tasks() {
        return tasks;
    }

    @Override
    public Representation representation() { return representation; }

    @Override
    public String name() { return name; }

    @Override
    public JSONObject serialize() throws JSONException {

        // Serialize tasks
        JSONArray json_tasks = new JSONArray();

        return new JSONObject()
                .put("name", name)
                .put("class", getClass().getSimpleName())
                .put("grid", grid.serialize())
                .put("map", map)
                .put("machines", machines)
                .put("tasks", tasks);
    }

    public static FarmWorld load(JSONObject config) throws JSONException {
        String name = config.getString("name");
        NavGrid grid = NavGrid.load(config.getJSONObject("grid"));

        Terrain[][] map = new Terrain[grid.height()][grid.width()];
        JSONArray map_rows = config.getJSONArray("map");

        for(int row = 0; row < grid.height(); ++row) {
            JSONArray map_columns = map_rows.getJSONArray(row);

            for(int column = 0; column < grid.width(); ++column)
                map[row][column] = map_columns.getEnum(Terrain.class, column);
        }

        Machine[][] machines = new Machine[grid.height()][grid.width()];
        JSONArray machine_rows = config.getJSONArray("machines");

        for(int row = 0; row < grid.height(); ++row) {
            JSONArray machine_columns = machine_rows.getJSONArray(row);

            for(int column = 0; column < grid.width(); ++column)
                machines[row][column] = machine_columns.getEnum(Machine.class, column);
        }

        FarmWorld environment = new FarmWorld(name, grid, map, machines);

        JSONArray tasks = config.getJSONArray("tasks");

        for(int task = 0; task < tasks.length(); ++task)
            environment.addGoal(tasks.getJSONObject(task));

        return environment;
    }
}
