package bam.domains.gravity_world;

import bam.Dynamics;
import bam.Environment;
import bam.Representation;
import bam.Task;
import bam.domains.NavGrid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class GravityEnvironment implements Environment {

    private final String name;

    private final int[][] colors;
    private final int[] gravity;
    private final NavGrid grid;
    private final int depth;

    private final List<Task> tasks;

    private final GravityDynamics dynamics;
    private final GravityRepresentation representation;

    GravityEnvironment(String name, NavGrid grid, int[][] colors, int[] gravity) {
        this.name = name;
        this.grid = grid;
        this.colors = colors;
        this.gravity = gravity;

        depth = 4 * (grid.width() + grid.height());

        tasks = new LinkedList<>();

        dynamics = new GravityDynamics(grid, colors, gravity, depth);
        representation = new GravityRepresentation(grid, colors, depth);
    }

    /**
     * Adds a new task with a goal at the given row and column.
     *
     * @param name the name of this task
     * @param row the row of the goal
     * @param column the column of the goal
     */
    public void addGoal(String name, int row, int column) {

        // Initialize reward function
        final double[] rewards = new double[grid.numCells()];
        Arrays.fill(rewards, 0.0);

        // Set goal reward
        rewards[grid.index(row, column)] = 1.0;

        // Add task
        tasks.add(new Task() {

            @Override
            public int initial(Random random) {
                int row = random.nextInt(grid.height());
                int col = random.nextInt(grid.width());
                int gravity = random.nextInt(4);

                return (gravity * grid.numCells()) + grid.index(row, col);
            }

            @Override
            public double reward(int state) {
                return rewards[state % grid.numCells()];
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
        });
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
                .put("width", grid.width())
                .put("height", grid.height())
                .put("gravity", gravity)
                .put("tasks", tsks);
    }

    @Override
    public Optional<? extends BufferedImage> render() {
        BufferedImage image = new BufferedImage(grid.width() * GravityWorld.SCALE,
                grid.height() * GravityWorld.SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.translate(0, image.getHeight());
        graphics.scale(1, -1);

        graphics.setPaint(new Color(85,105,205));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        Color[] palette = new Color[GravityWorld.CLEAR];
        palette[GravityWorld.BLUE] = Color.BLUE;
        palette[GravityWorld.ORANGE] = Color.ORANGE;
        palette[GravityWorld.GREEN] = Color.GREEN;
        palette[GravityWorld.PURPLE] = Color.MAGENTA;

        Shape[] shapes = new Shape[4];
        shapes[GravityWorld.NORTH] = new Polygon(new int[] {0, GravityWorld.SCALE / 2, GravityWorld.SCALE},
                new int[]{0, GravityWorld.SCALE, 0}, 3);
        shapes[GravityWorld.SOUTH] = new Polygon(new int[] {0, GravityWorld.SCALE / 2, GravityWorld.SCALE},
                new int[]{GravityWorld.SCALE, 0, GravityWorld.SCALE}, 3);
        shapes[GravityWorld.EAST] = new Polygon(new int[]{0, GravityWorld.SCALE, 0},
                new int[] {0, GravityWorld.SCALE / 2, GravityWorld.SCALE}, 3);
        shapes[GravityWorld.WEST] = new Polygon(new int[]{GravityWorld.SCALE, 0, GravityWorld.SCALE},
                new int[] {0, GravityWorld.SCALE / 2, GravityWorld.SCALE}, 3);

        for(int row = 0; row < grid.height(); ++row)
            for(int column = 0; column < grid.width(); ++column)
                if(GravityWorld.CLEAR != colors[row][column]) {
                    int color = colors[row][column];
                    int grav = gravity[color];

                    graphics.setPaint(palette[color]);
                    graphics.fill(AffineTransform
                            .getTranslateInstance(column * GravityWorld.SCALE, row * GravityWorld.SCALE)
                            .createTransformedShape(shapes[grav]));
                }

        return Optional.of(image);
    }
}
