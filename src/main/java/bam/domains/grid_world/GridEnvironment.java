package bam.domains.grid_world;

import bam.Dynamics;
import bam.Environment;
import bam.Representation;
import bam.Task;
import bam.domains.NavGrid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class GridEnvironment implements Environment {

    private final String name;

    private final boolean[][] map;
    private final NavGrid grid;
    private final int depth;

    private final List<Task> tasks;

    private final GridDynamics dynamics;
    private final GridRepresentation representation;

    GridEnvironment(String name, NavGrid grid, boolean[][] map) {
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

        // Initialize reward function
        final double[] rewards = new double[grid.numCells()];
        Arrays.fill(rewards, 0.0);

        // Set goal reward
        rewards[grid.index(row, column)] = 1.0;

        // Add task
        tasks.add(new Task() {

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
                .put("actions", grid.numMoves())
                .put("tasks", tsks);
    }

    @Override
    public Optional<? extends BufferedImage> render() {
        BufferedImage image = new BufferedImage(grid.width() * GridWorld.SCALE,
                grid.height() * GridWorld.SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.translate(0, image.getHeight());
        graphics.scale(1, -1);

        graphics.setPaint(new Color(65,105,225));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        graphics.setPaint(new Color(255, 255, 255));

        for(int row = 0; row < grid.height(); ++row)
            for(int column = 0; column < grid.width(); ++column)
                if(map[row][column])
                    graphics.fillRect(column * GridWorld.SCALE,
                            row * GridWorld.SCALE, GridWorld.SCALE, GridWorld.SCALE);

        return Optional.of(image);
    }
}
