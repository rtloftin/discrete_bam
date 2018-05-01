package bam.simulation;

import bam.algorithms.Agent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a the data collected
 * during a single simulated session
 * with a single agent.
 */
public class Session {

    private final Agent agent;
    private final List<Double> episodes;

    private Session(Agent agent) {
        this.agent = agent;

        episodes = new ArrayList<>();
    }

    public static Session with(Agent agent) {
        return new Session(agent);
    }

    public Agent agent() {
        return agent;
    }

    public void episode(double performance) {
        episodes.add(performance);
    }

    public int episodes() {
        return episodes.size();
    }

    public double performance() {
        return episodes.get(episodes.size() - 1);
    }

    public double performance(int index) {
        return episodes.get(index);
    }

    public JSONObject serialize() throws JSONException {
        JSONArray json_episodes = new JSONArray();

        for(double episode : episodes)
            json_episodes.put(episode);

        return new JSONObject().put("episodes", json_episodes);
    }
}
