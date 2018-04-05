package bam.algorithms;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Behavior {

    private HashMap<String, double[][]> policies;

    private Behavior() { policies = new HashMap<>(); }

    public static Behavior get() { return new Behavior(); }

    public static Behavior load(JSONObject config) throws JSONException {
        Behavior behavior = new Behavior();
        JSONArray tasks = config.getJSONArray("tasks");

        for(int i=0; i < tasks.length(); ++i) {
            JSONObject task = tasks.getJSONObject(i);

            String name = task.getString("name");
            JSONArray states = task.getJSONArray("policy");
            double[][] policy = new double[states.length()][];

            for(int state = 0; state < states.length(); ++state) {
                JSONArray actions = states.getJSONArray(state);
                policy[state] = new double[actions.length()];

                for(int action = 0; action < actions.length(); ++action)
                    policy[state][action] = actions.getDouble(action);
            }

            behavior.put(name, policy);
        }

        return behavior;
    }

    public Behavior put(String task, double[][] policy) {
        policies.put(task, policy);

        return this;
    }

    public double[][] get(String task) {
        return policies.get(task);
    }

    public Policy policy(String task) {
        double[][] policy = policies.get(task);

        if(null == policy)
            return null;

        return (int state) -> policy[state];
    }

    public JSONObject serialize() throws JSONException {
        JSONArray tasks = new JSONArray();

        for(String task : policies.keySet()) {
            double[][] policy = policies.get(task);
            JSONArray states = new JSONArray();

            for(int state = 0; state < policy.length; ++state) {
                JSONArray actions = new JSONArray();

                for(int action = 0; action < policy[state].length; ++action)
                    actions.put(policy[state][action]);

                states.put(actions);
            }

            tasks.put(new JSONObject() // May want to change the JSON API we are using, the current one is limited
                    .put("name", task)
                    .put("policy", states));
        }

        return new JSONObject().put("tasks", tasks);
    }
}
