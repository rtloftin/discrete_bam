package bam.domains;

import bam.algorithms.*;
import org.json.JSONException;
import org.json.JSONObject;

public class Experts implements Agent {

    public static Algorithm algorithm(Environment environment) {
        Behavior behavior = Behavior.get();

        for(Task task : environment.tasks())
            behavior.put(task.name(), Expert.with(environment.dynamics(), task).policy());

        return new Algorithm() {
            @Override
            public Agent agent(Representation representation) {
                return new Experts(behavior);
            }

            @Override
            public String name() {
                return "Expert";
            }

            @Override
            public JSONObject serialize() throws JSONException {
                return new JSONObject()
                        .put("name", name())
                        .put("class", Experts.class.getSimpleName())
                        .put("behavior", behavior.serialize());
            }
        };
    }

    private Behavior behavior;
    private double[][] policy = null;

    private Experts(Behavior behavior) { this.behavior = behavior; }

    @Override
    public void task(String name) {
        policy = behavior.get(name);
    }

    @Override
    public void observe(TeacherAction action) { /* Does nothing */ }

    @Override
    public void observe(TeacherFeedback feedback) { /* Does nothing */ }

    @Override
    public void observe(StateTransition transition) { /* Does nothing */ }

    @Override
    public Behavior integrate() {
        return behavior;
    }

    @Override
    public double[] policy(int state) {
        return policy[state];
    }
}
