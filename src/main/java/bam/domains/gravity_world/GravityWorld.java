package bam.domains.gravity_world;

import bam.algorithms.Dynamics;
import bam.domains.Environment;
import bam.algorithms.Representation;
import bam.domains.NavGrid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class GravityWorld implements Environment {

    // The size of each grid cell in pixels, for visualization.
    static final int SCALE = 40;

    public class Task implements bam.domains.Task {

        private final int row, column;
        private final int gravity;
        private final String name;
        private final double[] rewards;

        private Task(String name, int row, int column, int gravity) {
            this.name = name;
            this.row = row;
            this.column = column;
            this.gravity = gravity;

            // Initialize reward function
            rewards = new double[grid.numCells()];
            Arrays.fill(rewards, 0.0);

            // Set goal reward
            rewards[grid.index(row, column)] = 1.0;
        }

        private Task(JSONObject config) throws JSONException {
            this(config.getString("name"), config.getInt("row"),
                    config.getInt("column"), config.optInt("gravity", -1));
        }

        public int row() {
            return row;
        }

        public int column() {
            return column;
        }

        @Override
        public int initial(Random random) {

            int row = random.nextInt(grid.height());
            int col = random.nextInt(grid.width());
            int grav = (-1 == gravity) ? random.nextInt(Gravity.values().length) : gravity;

            return (grav * grid.numCells()) + grid.index(row, col);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public double reward(int state) {
            return rewards[state % grid.numCells()];
        }

        @Override
        public JSONObject serialize() throws JSONException {
            return new JSONObject()
                    .put("name", name())
                    .put("row", row)
                    .put("column", column)
                    .put("gravity", gravity);
        }
    }

    private final String name;

    private final Colors[][] colors;
    private final Gravity[] gravity;
    private final NavGrid grid;
    private final int depth;

    private final List<Task> tasks;

    private final GravityDynamics dynamics;
    private final GravityRepresentation representation;

    GravityWorld(String name, NavGrid grid, Colors[][] colors, Gravity[] gravity) {
        this.name = name;
        this.grid = grid;
        this.colors = colors;
        this.gravity = gravity;

        depth = 4 * (grid.width() + grid.height());

        tasks = new LinkedList<>();

        dynamics = new GravityDynamics(grid, colors, gravity, depth);
        representation = new GravityRepresentation(grid, colors, depth);
    }

    private void addGoal(JSONObject config) throws JSONException { tasks.add(this.new Task(config)); }

    /**
     * Adds a new task with a goal at the given row and column.
     *
     * @param name the name of this task
     * @param row the row of the goal
     * @param column the column of the goal
     */
    public void addGoal(String name, int row, int column) {
        tasks.add(this.new Task(name, row, column, -1));
    }

    /**
     * Adds a new task with a goal at the given row and column.
     *
     * @param name the name of this task
     * @param row the row of the goal
     * @param column the column of the goal
     * @param gravity the initial gravity direction
     */
    public void addGoal(String name, int row, int column, int gravity) {
        tasks.add(this.new Task(name, row, column, gravity));
    }

    public int width() {
        return grid.width();
    }

    public int height() {
        return grid.height();
    }

    public int index(int row, int column, Gravity gravity) {
        return (gravity.ordinal() * grid.numCells()) + grid.index(row, column);
    }

    public int row(int state) {
        return grid.row(state % grid.numCells());
    }

    public int column(int state) {
        return grid.column(state % grid.numCells());
    }

    public Gravity gravity(int state) { return Gravity.values()[state / grid.numCells()]; }

    public Gravity[] gravity() { return gravity; }

    public Colors[][] colors() { return colors; }

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
        BufferedImage image = new BufferedImage(grid.width() * SCALE,
                grid.height() * SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.translate(0, image.getHeight());
        graphics.scale(1, -1);

        graphics.setPaint(new Color(85,105,205));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        Shape[] shapes = new Shape[4];
        shapes[Gravity.NORTH.ordinal()] = new Polygon(new int[] {0, SCALE / 2, SCALE}, new int[]{SCALE, 0, SCALE}, 3);
        shapes[Gravity.SOUTH.ordinal()] = new Polygon(new int[] {0, SCALE / 2, SCALE}, new int[]{0, SCALE, 0}, 3);
        shapes[Gravity.EAST.ordinal()] = new Polygon(new int[]{0, SCALE, 0}, new int[] {0, SCALE / 2, SCALE}, 3);
        shapes[Gravity.WEST.ordinal()] = new Polygon(new int[]{SCALE, 0, SCALE}, new int[] {0, SCALE / 2, SCALE}, 3);

        for(int row = 0; row < grid.height(); ++row)
            for(int column = 0; column < grid.width(); ++column) {
                Colors color = colors[row][column];

                if(Colors.CLEAR != color) {
                    graphics.setPaint(color.paint);
                    graphics.fill(AffineTransform
                            .getTranslateInstance(column * SCALE, row * SCALE)
                            .createTransformedShape(shapes[gravity[color.ordinal()].ordinal()]));
                }
            }

        return Optional.of(image);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public JSONObject serialize() throws JSONException {
        JSONArray tsks  =new JSONArray();

        for(Task task : tasks)
            tsks.put(task.serialize());

        return new JSONObject()
                .put("name", name())
                .put("class", getClass().getSimpleName())
                .put("grid", grid.serialize())
                .put("colors", new JSONArray(colors))
                .put("gravity", new JSONArray(gravity))
                .put("tasks", tsks);
    }

    public static GravityWorld load(JSONObject config) throws JSONException {
        String name = config.getString("name");
        NavGrid grid = NavGrid.load(config.getJSONObject("grid"));

        Colors[][] colors = new Colors[grid.height()][grid.width()];
        JSONArray rows = config.getJSONArray("colors");

        for(int row = 0; row < grid.height(); ++row) {
            JSONArray columns = rows.getJSONArray(row);

            for(int column = 0; column < grid.width(); ++column)
                colors[row][column] = columns.getEnum(Colors.class, column);
        }

        Gravity[] gravity = new Gravity[Colors.values().length];
        JSONArray mapping = config.getJSONArray("gravity");

        for(int color = 0; color < Colors.values().length; ++color)
            gravity[color] = mapping.getEnum(Gravity.class, color);

        GravityWorld environment = new GravityWorld(name, grid, colors, gravity);

        JSONArray tasks = config.getJSONArray("tasks");

        for(int task = 0; task < tasks.length(); ++task)
            environment.addGoal(tasks.getJSONObject(task));

        return environment;
    }
}
