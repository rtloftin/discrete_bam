package bam.human.domains;

import bam.algorithms.Agent;
import bam.algorithms.Algorithm;
import bam.algorithms.StateTransition;
import bam.algorithms.TeacherAction;
import bam.domains.FiniteSimulation;
import bam.domains.NavGrid;
import bam.domains.Task;
import bam.domains.grid_world.GridWorld;
import bam.human.Remote;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ThreadLocalRandom;

public class RemoteGridWorld implements Remote {

    // The agent being trained (may be a dummy agent, such as an expert)
    private final Agent agent;

    // The grid world environment being represented
    private final GridWorld environment;

    // The current simulation
    private final FiniteSimulation simulation;

    private GridWorld.Task current_task; // The current task
    private String direction; // The direction the robot is currently facing

    private RemoteGridWorld(GridWorld environment, Agent agent, JSONObject initial) throws JSONException {
        this.environment = environment;
        this.agent = agent;

        // Initialize simulation
        simulation = FiniteSimulation.of(environment.dynamics(), agent);

        // Set initial task
        if(initial.has("task"))
            setTask(initial.getJSONObject("task"));
        else { // Clearly there are flaws in the current implementation, but we can deal with them later on
            current_task = environment.tasks().get(0);
            simulation.setTask(current_task);
        }

        // Set initial state
        if(initial.has("state"))
            setState(initial.getJSONObject("state"));
        else
            resetState();
    }

    public static RemoteGridWorld with(GridWorld environment, Agent agent, JSONObject initial) throws JSONException {
        return new RemoteGridWorld(environment, agent, initial);
    }

    public static Remote.Factory with(GridWorld environment) {
        return new Factory() {
            @Override
            public Remote build(Algorithm algorithm, JSONObject initial) throws JSONException {
                return with(environment, algorithm.agent(environment.representation()), initial);
            }

            @Override
            public JSONObject serialize() throws JSONException {
                return environment.serialize();
            }
        };
    }

    public static Remote.Factory load(JSONObject config) throws JSONException {
        return with(GridWorld.load(config));
    }

    @Override
    public synchronized JSONObject integrate() throws JSONException {
        return agent.integrate().serialize();
    }

    @Override
    public synchronized void setTask(JSONObject task) throws JSONException {
        if(task.has("name")) {
            String name = task.getString("name");

            // Find the task with the same name
            for(GridWorld.Task next_task : environment.tasks())
                if(next_task.name().equals(name)) {

                    // Change the current task
                    current_task = next_task;

                    // Set the new task
                    simulation.setTask(current_task);

                    // Stop on the first task with this name
                    break;
                }
        }
    }

    @Override
    public synchronized void setState(JSONObject state) throws JSONException {
        int row = state.getInt("y");
        int column = state.getInt("x");
        direction = state.getString("direction");

        simulation.setState(environment.index(row, column));
    }

    @Override
    public synchronized void resetState() {
        simulation.reset();

        int state = simulation.getState();
        int row_offset = environment.row(state) - (environment.height() / 2);
        int column_offset = environment.column(state) - (environment.width() / 2);

        if(row_offset <= 0) {
            direction = "down";

            if(column_offset < row_offset)
                direction = "right";
            else if(column_offset > -row_offset)
                direction = "left";
        }
        else {
            direction = "up";

            if(column_offset < -row_offset)
                direction = "right";
            else if(column_offset > row_offset)
                direction = "left";
        }
    }

    @Override
    public synchronized void takeAction(JSONObject action, boolean on_task) throws JSONException {

        // Parse action
        String action_type = action.getString("type");
        int action_index = NavGrid.STAY;

        if(action_type.equals("up")) {
            action_index = NavGrid.UP;
            direction = "up";
        }
        else if(action_type.equals("down")) {
            action_index = NavGrid.DOWN;
            direction = "down";
        }
        else if(action_type.equals("left")) {
            action_index = NavGrid.LEFT;
            direction = "left";
        }
        else if(action_type.equals("right")) {
            action_index = NavGrid.RIGHT;
            direction = "right";
        }

        // Take action
        simulation.takeAction(action_index, on_task);
    }

    @Override
    public synchronized void takeAction() {

        simulation.takeAction();

        int action = simulation.getAction();

        if(NavGrid.UP == action)
            direction = "up";
        else if(NavGrid.DOWN == action)
            direction = "down";
        else if(NavGrid.LEFT == action)
            direction = "left";
        else if(NavGrid.RIGHT == action)
            direction = "right";
    }

    @Override
    public synchronized void giveFeedback(JSONObject feedback) throws JSONException {
        String type = feedback.optString("type", "none");
        double value = 0.0;

        if(type.equals("reward")) {
            value = 1.0;
        } else if(type.equals("punishment")) {
            value = -1.0;
        }

        simulation.giveFeedback(value);
    }

    @Override
    public synchronized int getDepth() {
        return environment.dynamics().depth();
    }

    @Override
    public synchronized JSONArray getTasks() throws JSONException {
        JSONArray tasks = new JSONArray();

        for(Task task : environment.tasks())
            tasks.put(new JSONObject()
                    .put("name", task.name())
                    .put("display_name", task.name()));

        return tasks;
    }

    @Override
    public synchronized JSONObject getClientLayout() throws JSONException {
        return new JSONObject()
                .put("width", environment.width())
                .put("height", environment.height())
                .put("map", new JSONArray(environment.map()));
    }

    @Override
    public synchronized JSONObject getClientTask() throws JSONException {
        return new JSONObject()
                .put("x", current_task.column())
                .put("y", current_task.row())
                .put("name", current_task.name());
    }

    @Override
    public synchronized JSONObject getClientState() throws JSONException {
        JSONObject state = new JSONObject();
        state.put("x", environment.column(simulation.getState()));
        state.put("y", environment.row(simulation.getState()));
        state.put("direction", direction);

        return state;
    }

    @Override
    public synchronized JSONObject getState() throws JSONException {
        return new JSONObject().put("state", simulation.getState());
    }

    @Override
    public synchronized JSONObject getAction() throws JSONException {
        return new JSONObject().put("action", simulation.getAction());
    }
}
