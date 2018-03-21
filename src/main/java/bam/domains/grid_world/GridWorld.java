package bam.domains.grid_world;

import bam.algorithms.Dynamics;
import bam.domains.Environment;
import bam.algorithms.Representation;
import bam.domains.Task;
import bam.domains.NavGrid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class GridWorld implements Environment {

    public class Task implements bam.domains.Task {

        private final int row, column;
        private final String name;
        private final double[] rewards;

        private Task(String name, int row, int column) {
            this.name = name;
            this.row = row;
            this.column = column;

            // Initialize reward function
            rewards = new double[grid.numCells()];
            Arrays.fill(rewards, 0.0);

            // Set goal reward
            rewards[grid.index(row, column)] = 1.0;
        }

        public int row() {
            return row;
        }

        public int column() {
            return column;
        }

        @Override
        public int initial(Random random) {
            int row, col;

            do {
                row = random.nextInt(grid.height());
                col = random.nextInt(grid.width());
            } while(map[row][col]);

            return grid.index(row, col);
        }

        @Override
        public double reward(int state) {
            return rewards[state];
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public JSONObject serialize() throws JSONException {
            return new JSONObject()
                    .put("name", name())
                    .put("goal row", row)
                    .put("goal column", column);
        }
    }

    private final String name;

    private final boolean[][] map;
    private final NavGrid grid;
    private final int depth;

    private final List<Task> tasks;

    private final GridDynamics dynamics;
    private final GridRepresentation representation;

    GridWorld(String name, NavGrid grid, boolean[][] map) {
        this.name = name;
        this.grid = grid;
        this.map = map;

        depth = 2 * (grid.width() + grid.height());

        tasks = new LinkedList<>();

        dynamics = new GridDynamics(grid, map, depth);
        representation = new GridRepresentation(grid, depth);
    }

    /**
     * Adds a new task with a goal at the given row and column.
     *
     * @param name the name of this task
     * @param row the row of the goal
     * @param column the column of the goal
     */
    public void addGoal(String name, int row, int column) {
        tasks.add(new Task(name, row, column));
    }

    /**
     * Adds a new goal based on its JSON representation.
     *
     * @param config the JSON representation of the goal
     * @throws JSONException
     */
    public void addGoal(JSONObject config) throws JSONException {

    }

    public int width() {
        return grid.width();
    }

    public int height() {
        return grid.height();
    }

    public int index(int row, int column) {
        return grid.index(row, column);
    }

    public int row(int index) {
        return grid.row(index);
    }

    public int column(int index) {
        return grid.column(index);
    }

    public boolean occupied(int row, int column) {
        return map[row][column];
    }

    @Override
    public Dynamics dynamics() {
        return dynamics;
    }

    @Override
    public List<? extends Task> tasks() {
        return tasks;
    }

    @Override
    public Representation representation() {
        return representation;
    }

    @Override
    public Optional<? extends BufferedImage> render() {
        BufferedImage image = new BufferedImage(grid.width() * GridWorlds.SCALE,
                grid.height() * GridWorlds.SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.translate(0, image.getHeight());
        graphics.scale(1, -1);

        graphics.setPaint(new Color(65,105,225));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        graphics.setPaint(new Color(255, 255, 255));

        for(int row = 0; row < grid.height(); ++row)
            for(int column = 0; column < grid.width(); ++column)
                if(map[row][column])
                    graphics.fillRect(column * GridWorlds.SCALE,
                            row * GridWorlds.SCALE, GridWorlds.SCALE, GridWorlds.SCALE);

        return Optional.of(image);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public JSONObject serialize() throws JSONException {

        // Serialize map
        JSONArray rows = new JSONArray();

        for(int row = 0; row < grid.height(); ++row) {
            JSONArray columns = new JSONArray();

            for(int column = 0; column < grid.width(); ++column)
                columns.put(map[row][column]);

            rows.put(columns);
        }

        // Serialize tasks
        JSONArray json_tasks = new JSONArray();

        for(Task task : tasks)
            json_tasks.put(task.serialize());

        return new JSONObject()
                .put("name", name())
                .put("class", getClass().getSimpleName())
                .put("grid", grid.serialize())
                .put("map", rows)
                .put("tasks", json_tasks);
    }

    public static GridWorld load(JSONObject config) throws JSONException {
        String name = config.getString("name");
        NavGrid grid = NavGrid.load(config.getJSONObject("grid"));

        boolean[][] map = new boolean[grid.height()][grid.width()];
        JSONArray rows = config.getJSONArray("map");

        for(int row = 0; row < grid.height(); ++row) {
            JSONArray columns = rows.getJSONArray(row);

            for(int column = 0; column < grid.width(); ++column)
                map[row][column] = columns.getBoolean(column);
        }

        GridWorld environment = new GridWorld(name, grid, map);

        JSONArray tasks = config.getJSONArray("tasks");

        for(int task = 0; task < tasks.length(); ++task)
            environment.addGoal(tasks.getJSONObject(task));

        return environment;
    }
}
