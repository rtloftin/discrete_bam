package bam.human.domains;

import bam.human.Remote;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoteGravityWorld implements Remote {

    @Override
    public void integrate() {

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
