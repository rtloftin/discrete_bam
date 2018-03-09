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
        JSONArray json_tasks  =new JSONArray();

        for(Task task : tasks)
            json_tasks.put(task.serialize());

        return new JSONObject()
                .put("name", name())
                .put("width", grid.width())
                .put("height", grid.height())
                .put("actions", grid.numMoves())
                .put("tasks", json_tasks);
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

    //////////////////////////////////////////
    // Serialization Methods for the Server //
    //////////////////////////////////////////


    @Override
    public int parseAction(JSONObject action) throws JSONException {
        String type = action.getString("type");

        if(type.equals("up"))
            return NavGrid.UP;
        if(type.equals("down"))
            return NavGrid.DOWN;
        if(type.equals("left"))
            return NavGrid.LEFT;
        if(type.equals("right"))
            return NavGrid.RIGHT;

        return NavGrid.STAY;
    }

    @Override
    public int parseState(JSONObject state) throws JSONException {
        int column = state.getInt("x");
        int row = state.getInt("y");

        return grid.index(row, column);
    }

    @Override
    public JSONObject writeState(int state, int last_action) throws JSONException {
        int x = grid.column(state);
        int y = grid.row(state);

        double theta = 0.0;

        if(NavGrid.DOWN == last_action)
            theta = Math.PI;
        else if(NavGrid.LEFT == last_action)
            theta = 0.5 * Math.PI;
        else if(NavGrid.RIGHT == last_action)
            theta = -0.5 * Math.PI;

        JSONObject json = new JSONObject();
        json.put("x", x);
        json.put("y", y);

        return json;
    }

    @Override
    public JSONObject writeLayout(String task) throws JSONException {
        JSONObject json = new JSONObject();

        // This isn't going to work, need to rethink this

        return null;
    }
}
