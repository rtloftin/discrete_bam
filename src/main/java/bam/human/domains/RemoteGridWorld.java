package bam.human.domains;

import bam.algorithms.Agent;
import bam.algorithms.Algorithm;
import bam.algorithms.StateTransition;
import bam.algorithms.TeacherAction;
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
    private Agent agent;

    // The grid world environment being represented
    private GridWorld environment;

    // The current task
    private GridWorld.Task current_task;

    private int current_state; // The index of the robot's current cell
    private String direction; // The direction the robot is currently facing

    private RemoteGridWorld(GridWorld environment, Agent agent, JSONObject initial) throws JSONException {
        this.environment = environment;
        this.agent = agent;

        // Set initial task
        if(initial.has("task"))
            setTask(initial.getJSONObject("task"));
        else
            setTask(environment.tasks().get(0).name());

        // Set initial state
        if(initial.has("state"))
            setState(initial);
        else
            resetState();
    }

    private void setTask(String name) {

        // Tell the agent
        agent.task(name);

        // Change the task -- find the first matching task
        for(GridWorld.Task next_task : environment.tasks())
            if(next_task.name().equals(name)) {
                current_task = next_task;
                break;
            }
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
    public JSONObject integrate() throws JSONException {
        return agent.integrate().serialize();
    }

    @Override
    public synchronized void setTask(JSONObject task) throws JSONException {
        setTask(task.getString("name"));
    }

    @Override
    public synchronized void setState(JSONObject state) throws JSONException {
        int row = state.getInt("y");
        int column = state.getInt("x");
        direction = state.getString("direction");

        current_state = environment.index(row, column);
    }

    @Override
    public synchronized void resetState() {
        current_state = current_task.initial(ThreadLocalRandom.current());

        int row_offset = environment.row(current_state) - (environment.height() / 2);
        int column_offset = environment.column(current_state) - (environment.width() / 2);

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
    public synchronized void takeAction(JSONObject action) throws JSONException {

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

        // Show action to agent
        agent.observe(TeacherAction.of(current_state, action_index));

        // Compute next state
        int next_state = environment.dynamics()
                .transition(current_state, action_index, ThreadLocalRandom.current());

        // Show transition to agent
        agent.observe(StateTransition.of(current_state, action_index, next_state));

        // Update state
        current_state = next_state;
    }

    @Override
    public synchronized void takeAction() {

        // Get action from agent
        int action = agent.action(current_state, ThreadLocalRandom.current());

        if(NavGrid.UP == action)
            direction = "up";
        else if(NavGrid.DOWN == action)
            direction = "down";
        else if(NavGrid.LEFT == action)
            direction = "left";
        else if(NavGrid.RIGHT == action)
            direction = "right";

        // Compute next state
        int next_state = environment.dynamics()
                .transition(current_state, action, ThreadLocalRandom.current());

        // Sow transition to agent
        agent.observe(StateTransition.of(current_state, action, next_state));

        // Update state
        current_state = next_state;
    }

    @Override
    public int getDepth() {
        return environment.dynamics().depth();
    }

    @Override
    public JSONArray getTasks() throws JSONException {
        JSONArray tasks = new JSONArray();

        for(Task task : environment.tasks())
            tasks.put(new JSONObject()
                    .put("name", task.name())
                    .put("display_name", task.name()));

        return tasks;
    }

    @Override
    public synchronized JSONObject getLayout() throws JSONException {

        // Write task
        JSONObject task = new JSONObject();
        task.put("x", current_task.column());
        task.put("y", current_task.row());

        // Return layout representation
        return new JSONObject()
                .put("width", environment.width())
                .put("height", environment.height())
                .put("map", new JSONArray(environment.map()))
                .put("task", task);
    }

    @Override
    public synchronized JSONObject getState() throws JSONException {
        JSONObject state = new JSONObject();
        state.put("x", environment.column(current_state));
        state.put("y", environment.row(current_state));
        state.put("direction", direction);

        return state;
    }
}
