package bam.human.domains;

import bam.algorithms.Agent;
import bam.domains.farm_world.FarmWorld;
import bam.human.Remote;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoteFarmWorld implements Remote {

    // The agent being trained (may be a dummy agent, such as an expert)
    private Agent agent;

    // The grid world environment being represented
    private FarmWorld environment;

    // The current task
   // private GridWorld.Task current_task;

    // The current state index
    private int current_state;

    @Override
    public JSONObject integrate() throws JSONException {
        return null;
    }

    @Override
    public void setTask(JSONObject task) throws JSONException {

    }

    @Override
    public void setState(JSONObject state) throws JSONException {

    }

    @Override
    public void resetState() {

    }

    @Override
    public void takeAction(JSONObject action) throws JSONException {

    }

    @Override
    public void takeAction() {

    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public JSONArray getTasks() throws JSONException {
        return null;
    }

    @Override
    public JSONObject getLayout() throws JSONException {
        return null;
    }

    @Override
    public JSONObject getState() throws JSONException {
        return null;
    }
}
