package bam.human.domains;

import bam.algorithms.Agent;
import bam.algorithms.Algorithm;
import bam.algorithms.StateTransition;
import bam.algorithms.TeacherAction;
import bam.domains.NavGrid;
import bam.domains.Task;
import bam.domains.farm_world.FarmWorld;
import bam.domains.farm_world.Machine;
import bam.human.Remote;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ThreadLocalRandom;

public class RemoteFarmWorld implements Remote {

    // The agent being trained (may be a dummy agent, such as an expert)
    private Agent agent;

    // The grid world environment being represented
    private FarmWorld environment;

    // The current task
    private FarmWorld.Task current_task;

    // The current state index
    private int current_state;

    private RemoteFarmWorld(FarmWorld environment, Agent agent, JSONObject initial) throws JSONException {
        this.environment = environment;
        this.agent = agent;

        // Set initial task
        if(initial.has("task"))
            setTask(initial.getJSONObject("task"));
        else
            setTask(environment.tasks().get(0).name());

        // Set initial state
        if(initial.has("state"))
            setState(initial.getJSONObject("state"));
        else
            resetState();
    }

    private void setTask(String name) {

        // Tell the agent
        agent.task(name);

        // Change the task -- find the first matching task
        for(FarmWorld.Task next_task : environment.tasks())
            if(next_task.name().equals(name)) {
                current_task = next_task;
                break;
            }
    }

    public static RemoteFarmWorld with(FarmWorld environment, Agent agent, JSONObject initial) throws JSONException {
        return new RemoteFarmWorld(environment, agent, initial);
    }

    public static Remote.Factory with(FarmWorld environment) {
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
        return with(FarmWorld.load(config));
    }

    @Override
    public JSONObject integrate() throws JSONException {
        return agent.integrate().serialize();
    }

    @Override
    public void setTask(JSONObject task) throws JSONException {
        setTask(task.getString("name"));
    }

    @Override
    public void setState(JSONObject state) throws JSONException {
        int row = state.getInt("y");
        int column = state.getInt("x");
        Machine machine = state.getEnum(Machine.class, "machine");

        current_state = environment.index(row, column, machine);
    }

    @Override
    public void resetState() {
        current_state = current_task.initial(ThreadLocalRandom.current());
    }

    @Override
    public void takeAction(JSONObject action) throws JSONException {
        // Parse action
        String action_type = action.getString("type");
        int action_index = NavGrid.STAY;

        if(action_type.equals("up")) {
            action_index = NavGrid.UP;
        } else if(action_type.equals("down")) {
            action_index = NavGrid.DOWN;
        } else if(action_type.equals("left")) {
            action_index = NavGrid.LEFT;
        } else if(action_type.equals("right")) {
            action_index = NavGrid.RIGHT;
        }

        // Show action to agent
        if(action.optBoolean("on-task", true))
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
    public void takeAction() {

        // Get action from agent
        int action = agent.action(current_state, ThreadLocalRandom.current());

        // Compute next state
        int next_state = environment.dynamics()
                .transition(current_state, action, ThreadLocalRandom.current());

        // Sow transition to agent
        agent.observe(StateTransition.of(current_state, action, next_state));

        // Update state
        current_state = next_state;
    }

    @Override
    public int getDepth() { return environment.dynamics().depth(); }

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
    public JSONObject getLayout() throws JSONException {

        // Write task
        JSONObject task = new JSONObject()
                .put("x", current_task.column())
                .put("y", current_task.row())
                .put("width", current_task.width())
                .put("height", current_task.height())
                .put("name", current_task.name());

        // Return layout representation
        return new JSONObject()
                .put("width", environment.width())
                .put("height", environment.height())
                .put("map", new JSONArray(environment.map()))
                .put("machines", new JSONArray(environment.machines()))
                .put("task", task);
    }

    @Override
    public JSONObject getState() throws JSONException {
        return new JSONObject()
                .put("x", environment.column(current_state))
                .put("y", environment.row(current_state))
                .put("machine", environment.machine(current_state));
    }
}
