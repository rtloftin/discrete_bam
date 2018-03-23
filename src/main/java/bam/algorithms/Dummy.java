package bam.algorithms;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Dummy implements Agent {

    private double[][] policy;

    private Dummy(Representation representation) {
        policy = new double[representation.numStates()][];

        for(int state = 0; state < policy.length; ++state) {
            policy[state] = new double[representation.numActions(state)];
            Arrays.fill(policy[state], 1.0 / policy[state].length);
        }
    }

    public static Dummy with(Representation representation) {
        return new Dummy(representation);
    }

    public static Algorithm algorithm() {
        return new Algorithm() {
            @Override
            public Agent agent(Representation representation) {
                return new Dummy(representation);
            }

            @Override
            public String name() {
                return "Dummy";
            }

            @Override
            public JSONObject serialize() throws JSONException {
                return new JSONObject()
                        .put("name", name())
                        .put("class", Dummy.class.getSimpleName());
            }
        };
    }

    @Override
    public void task(String name) { /* Does nothing */ }

    @Override
    public void observe(TeacherAction action) { /* Does nothing */ }

    @Override
    public void observe(TeacherFeedback feedback) { /* Does nothing */ }

    @Override
    public void observe(StateTransition transition) { /* Does nothing */ }

    @Override
    public Behavior integrate() { return Behavior.get(); }

    @Override
    public double[] policy(int state) {
        return policy[state];
    }
}
