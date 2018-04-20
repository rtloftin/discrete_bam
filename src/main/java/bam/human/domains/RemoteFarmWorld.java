package bam.human.domains;

import bam.algorithms.Agent;
import bam.algorithms.Algorithm;
import bam.algorithms.FiniteSimulation;
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
    private final Agent agent;

    // The grid world environment being represented
    private final FarmWorld environment;

    // The current simulation
    private final FiniteSimulation simulation;

    // The current task
    private FarmWorld.Task current_task;

    private RemoteFarmWorld(FarmWorld environment, Agent agent, JSONObject initial) throws JSONException {
        this.environment = environment;
        this.agent = agent;

        // Initialize simulation
        simulation = FiniteSimulation.of(environment.dynamics(), agent);

        // Set initial task
        if(initial.has("task"))
            setTask(initial.getJSONObject("task"));
        else {
            current_task = environment.tasks().get(0);
            simulation.setTask(current_task.name());
        }

        // Set initial state
        if(initial.has("state"))
            setState(initial.getJSONObject("state"));
        else
            resetState();
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
    public synchronized JSONObject integrate() throws JSONException {
        return agent.integrate().serialize();
    }

    @Override
    public synchronized void setTask(JSONObject task) throws JSONException {
        if(task.has("name")) {
            String name = task.getString("name");

            // Find the task with the same name
            for(FarmWorld.Task next_task : environment.tasks())
                if(next_task.name().equals(name)) {

                    // Change the current task
                    current_task = next_task;

                    // Set the new task
                    simulation.setTask(current_task.name());

                    // Stop on the first task with this name
                    break;
                }
        }
    }

    @Override
    public synchronized void setState(JSONObject state) throws JSONException {
        int row = state.getInt("y");
        int column = state.getInt("x");
        Machine machine = state.getEnum(Machine.class, "machine");

        simulation.setState(environment.index(row, column, machine));
    }

    @Override
    public synchronized void resetState() {
        simulation.setState(current_task.initial(ThreadLocalRandom.current()));
    }

    @Override
    public synchronized void takeAction(JSONObject action, boolean on_task) throws JSONException {

        // Parse action
        String action_type = action.getString("type");
        int action_index = NavGrid.STAY;

        switch (action_type) {
            case "up":
                action_index = NavGrid.UP;
                break;
            case "down":
                action_index = NavGrid.DOWN;
                break;
            case "left":
                action_index = NavGrid.LEFT;
                break;
            case "right":
                action_index = NavGrid.RIGHT;
                break;
        }

        // Take action
        simulation.takeAction(action_index, on_task);
    }

    @Override
    public synchronized void takeAction() {
        simulation.takeAction();
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
                .put("map", new JSONArray(environment.map()))
                .put("machines", new JSONArray(environment.machines()));
    }

    @Override
    public synchronized JSONObject getClientTask() throws JSONException {
        return new JSONObject()
                .put("x", current_task.column())
                .put("y", current_task.row())
                .put("width", current_task.width())
                .put("height", current_task.height())
                .put("name", current_task.name());
    }

    @Override
    public synchronized JSONObject getClientState() throws JSONException {
        return new JSONObject()
                .put("x", environment.column(simulation.getState()))
                .put("y", environment.row(simulation.getState()))
                .put("machine", environment.machine(simulation.getState()));
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
