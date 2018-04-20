package bam.algorithms;

import java.util.concurrent.ThreadLocalRandom;

public class FiniteSimulation {

    // The environment
    private final Dynamics dynamics;

    // The agent
    private final Agent agent;

    private int current_state;
    private int previous_state;
    private int previous_action;

    private FiniteSimulation(Dynamics dynamics, Agent agent) {
        this.agent = agent;
        this.dynamics = dynamics;
    }

    private void update(int action) {
        previous_action = action;
        previous_state = current_state;

        current_state = dynamics.transition(previous_state, previous_action, ThreadLocalRandom.current());
    }

    public static FiniteSimulation of(Dynamics dynamics, Agent agent) {
        return new FiniteSimulation(dynamics, agent);
    }

    public void setState(int state) {
        current_state = state;

        previous_state = -1;
        previous_action = -1;
    }


    public void setTask(String name) {
        agent.task(name);
    }

    public void takeAction(int action, boolean on_task) {
        update(action);

        if(on_task)
            agent.observe(TeacherAction.of(previous_state, previous_action));

        agent.observe(StateTransition.of(previous_state, previous_action, current_state));
    }

    public void takeAction() {
        update(agent.action(current_state, ThreadLocalRandom.current()));

        agent.observe(StateTransition.of(previous_state, previous_action, current_state));
    }

    public void giveFeedback(double feedback) {
        if(0 <= previous_action)
            agent.observe(TeacherFeedback.of(previous_state, previous_action, feedback));
    }

    public int getState() {
        return current_state;
    }

    public int getAction() {
        return previous_action;
    }
}
