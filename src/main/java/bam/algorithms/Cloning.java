package bam.algorithms;

import bam.*;
import bam.algorithms.action.ActionModel;
import bam.algorithms.action.BoltzmannActionModel;
import bam.algorithms.action.GreedyActionModel;
import bam.algorithms.feedback.FeedbackModel;
import bam.algorithms.feedback.NoFeedback;
import bam.algorithms.variational.Variational;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An implementation of tabular behavioral cloning.
 */
public class Cloning implements Agent {

    public static class Builder {

        // The number of updates to perform to integrate new data
        private int num_updates = 10;

        // Whether or not to reinitialize the parameters when new data is integrated
        private boolean reinitialize = false;

        // The name of this algorithm
        private String name = "Cloning";

        // The variational model we use to represent posteriors over intent vectors
        private Variational task_source = null;

        // The model of how the teacher selects their actions
        private ActionModel action_model = null;

        // The model of how the teacher provides feedback
        private FeedbackModel feedback_model = null;

        private Builder() {}

        public Builder name(String name) {
            this.name = name;

            return this;
        }

        public Builder numUpdates(int num_updates) {
            this.num_updates = num_updates;

            return this;
        }

        public Builder reinitialize(boolean reinitialize) {
            this.reinitialize = reinitialize;

            return this;
        }

        public Builder taskSource(Variational task_source) {
            this.task_source = task_source;

            return this;
        }

        public Builder actionModel(ActionModel action_model) {
            this.action_model = action_model;

            return this;
        }

        public Builder feedbackModel(FeedbackModel feedback_model) {
            this.feedback_model = feedback_model;

            return this;
        }

        public Algorithm build() {

            if(null == task_source)
                throw new RuntimeException("DUMBASS!!! No task distribution source defined");

            if(null == action_model)
                action_model = BoltzmannActionModel.get();

            if(null == feedback_model)
                feedback_model = NoFeedback.get();

            return new Algorithm() {

                @Override
                public Agent agent(Representation representation) {
                    return new Cloning(representation, Builder.this);
                }

                @Override
                public String name() {
                    return name;
                }

                @Override
                public JSONObject serialize() throws JSONException {
                    return new JSONObject()
                            .put("name", name())
                            .put("num updates", num_updates)
                            .put("reinitialize", reinitialize)
                            .put("task source", task_source.serialize())
                            .put("action model", action_model.serialize())
                            .put("feedback model", feedback_model.serialize());
                }
            };
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private class TaskModel {

        // Data specific to this task
        final List<TeacherFeedback> feedback;
        final List<TeacherAction> actions;

        // The name of this task
        final String name;

        // The posterior of the intent vector for this task
        Variational.Density intent;

        // The current policy for this task
        double[][] policy;

        TaskModel(String name) {
            this.name = name;

            // Construct teacher data structures
            feedback = new LinkedList<>();
            actions = new LinkedList<>();

            // Construct intent distribution
            intent = config.task_source.density(flat_buffer.length, ThreadLocalRandom.current());

            // Construct policy buffer
            policy = new double[value_buffer.length][];

            for(int state = 0; state < value_buffer.length; ++state)
                policy[state] = new double[value_buffer[state].length];

            // Initialize intent distribution and task policy
            initialize();
        }

        void initialize() {

            // Initialize intent distribution
            intent.initialize();

            // Initialize task policy to uniform random
            for(int state = 0; state < policy.length; ++state) {
                int actions = policy[state].length;
                double probability = 1.0 / actions;

                for(int action = 0; action < actions; ++action)
                    policy[state][action] = probability;
            }
        }

        // Propagates the data associated with this
        void update() {

            // Scale down by the number of variational samples
            double scale = 1.0 / intent.numSamples();

            // Iterate over all intent samples
            for(int sample = 0; sample < intent.numSamples(); ++sample) {

                // Get the next sample from the variational distribution
                intent.nextSample();
                double[] values = intent.value();

                // Unpack the flat value buffer
                for(int state = 0; state < value_buffer.length; ++state)
                    for(int action = 0; action < value_buffer[state].length; ++action)
                        value_buffer[state][action] = values[mapping[state][action]];

                // Initialize the gradient
                for(int state = 0; state < gradient_buffer.length; ++state)
                    Arrays.fill(gradient_buffer[state], 0.0);

                // Incorporate feedback
                for(TeacherFeedback feedback : feedback)
                    config.feedback_model.gradient(feedback.value,
                            feedback.action, value_buffer[feedback.state], gradient_buffer[feedback.state], scale);

                // Incorporate actions
                for(TeacherAction action : actions)
                    config.action_model.gradient(action.action,
                            value_buffer[action.state], gradient_buffer[action.state], scale);

                // Pack the gradient into the flat buffer
                for(int state = 0; state < gradient_buffer.length; ++state)
                    for(int action = 0; action < gradient_buffer[state].length; ++action)
                        flat_buffer[mapping[state][action]] = gradient_buffer[state][action];

                // propagate intent
                intent.train(flat_buffer);
            }

            intent.update();
        }

        void updatePolicy() {
            double[] mean = intent.mean();

            for(int state = 0; state < value_buffer.length; ++state)
                for(int action = 0; action < value_buffer[state].length; ++action)
                    value_buffer[state][action] = mean[mapping[state][action]];

            policy = GreedyActionModel.get().policy(value_buffer);
        }
    }

    // The builder object that generated this class, used for configuration
    private Builder config;

    // A jagged buffer for state-action values
    private final double[][] value_buffer;

    // A jagged buffer for state-action gradients
    private final double[][] gradient_buffer;

    // The mapping between the flat and jagged buffers
    private final int[][] mapping;

    // A flat buffer for state-action values and their gradients
    private final double[] flat_buffer;

    // The task models
    private final HashMap<String, TaskModel> tasks;

    // The current task
    private TaskModel task = null;

    private Cloning(Representation representation, Builder config) {
        this.config = config;

        // Initialize backpropagation buffers
        value_buffer = new double[representation.numStates()][];
        gradient_buffer = new double[representation.numStates()][];
        mapping = new int[representation.numStates()][];
        int counter = 0;

        for(int state = 0; state < representation.numStates(); ++state) {
            value_buffer[state] = new double[representation.numActions(state)];
            gradient_buffer[state] = new double[representation.numActions(state)];
            mapping[state] = new int[representation.numActions(state)];

            for(int action = 0; action < representation.numActions(state); ++action)
                mapping[state][action] = counter++;
        }

        flat_buffer = new double[counter];

        // Initialize task set
        tasks = new HashMap<>();
    }

    @Override
    public void task(String name) {
        if(!tasks.containsKey(name))
            tasks.put(name, this.new TaskModel(name));

        task = tasks.get(name);
    }

    @Override
    public double[] policy(int state) {
        if(null == task)
            throw new RuntimeException("No task set");

        return task.policy[state];
    }

    @Override
    public void observe(TeacherAction action) {
        if(null == task)
            throw new RuntimeException("No task set when action observed");

        task.actions.add(action);
    }

    @Override
    public void observe(TeacherFeedback feedback) {
        if(null == task)
            throw new RuntimeException("No task set when feedback observed");

        task.feedback.add(feedback);
    }

    @Override
    public void observe(StateTransition transition) { /* Does nothing, transition are ignored */}

    @Override
    public void integrate() {

        if(config.reinitialize)
            for(TaskModel task : tasks.values())
                task.initialize();

        for (int step = 0; step < config.num_updates; ++step)
            for (TaskModel task : tasks.values())
                task.update();

        for (TaskModel task : tasks.values())
            task.updatePolicy();
    }
}
