package bam.algorithms;

import bam.algorithms.action.ActionModel;
import bam.algorithms.action.BoltzmannActionModel;
import bam.algorithms.action.GreedyActionModel;
import bam.algorithms.feedback.FeedbackModel;
import bam.algorithms.feedback.NoFeedback;
import bam.algorithms.optimization.Optimization;
import bam.algorithms.planning.IntentGraph;
import bam.algorithms.planning.Planner;
import bam.algorithms.planning.PlanningAlgorithm;
import bam.algorithms.variational.Variational;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An algorithms that uses the ML-IRL
 * algorithm with a shared model
 * learned based on the observed
 * state transitions.
 */
public class ModelBased implements Agent {

    public static class Builder {

        // The number of dynamics updates to perform to integrate new data
        private int dynamics_updates = 1000;

        // The number of task updates to perform to integrate new data
        private int task_updates = 1000;

        // Whether or not to reinitialize the parameters when new data is integrated
        private boolean reinitialize = false;

        // The name of this algorithm
        private String name = "Model-Based";

        // The optimization method for learning the parameters of the dynamics model
        private Optimization dynamics_optimization = null;

        // The planning algorithm we assume the teacher is using
        private PlanningAlgorithm planning_algorithm = null;

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

        public Builder dynamicsUpdates(int dynamics_updates) {
            this.dynamics_updates = dynamics_updates;

            return this;
        }

        public Builder taskUpdates(int task_updates) {
            this.task_updates = task_updates;

            return this;
        }

        public Builder reinitialize(boolean reinitialize) {
            this.reinitialize = reinitialize;

            return this;
        }

        public Builder dynamicsOptimization(Optimization optimization) {
            dynamics_optimization = optimization;

            return this;
        }

        public Builder planningAlgorithm(PlanningAlgorithm planning_algorithm) {
            this.planning_algorithm = planning_algorithm;

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
            if(null == dynamics_optimization)
                throw new RuntimeException("DUMBASS!!! No dynamics optimization method defined");

            if(null == planning_algorithm)
                throw new RuntimeException("DUMBASS!!! No planning algorithm defined");

            if(null == task_source)
                throw new RuntimeException("DUMBASS!!! No task distribution source defined");

            if(null == action_model)
                action_model = BoltzmannActionModel.get();

            if(null == feedback_model)
                feedback_model = NoFeedback.get();

            return new Algorithm() {

                @Override
                public Agent agent(Representation representation) {
                    return new ModelBased(representation, Builder.this);
                }

                @Override
                public String name() {
                    return name;
                }

                @Override
                public JSONObject serialize() throws JSONException {
                    return new JSONObject()
                            .put("name", name())
                            .put("dynamics updates", dynamics_updates)
                            .put("task updates", task_updates)
                            .put("reinitialize", reinitialize)
                            .put("dynamics optimization", dynamics_optimization.serialize())
                            .put("planning algorithm", planning_algorithm.serialize())
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
            intent = config.task_source.density(rewards.intentSize(), ThreadLocalRandom.current());

            // Construct policy buffer
            policy = new double[dynamics.numStates()][];

            for(int state = 0; state < dynamics.numStates(); ++state)
                policy[state] = new double[dynamics.numActions(state)];

            // Initialize intent distribution and task policy
            initialize();
        }

        void initialize() {

            // Initialize intent distribution
            intent.initialize();

            // Initialize task policy to uniform random
            for(int state = 0; state < dynamics.numStates(); ++state) {
                int actions = dynamics.numActions(state);
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

                // Set the next sample as the intent
                intent.nextSample();
                graph.setIntent(intent.value());

                // Get Q-function from the planner
                double[][] Q = planner.values();

                // Initialize Jacobian
                for(int state = 0; state < jacobian.length; ++state)
                    Arrays.fill(jacobian[state], 0.0);

                // Incorporate feedback
                for(TeacherFeedback feedback : feedback)
                    config.feedback_model.gradient(feedback.value,
                            feedback.action, Q[feedback.state], jacobian[feedback.state], scale);

                // Incorporate actions
                for(TeacherAction action : actions)
                    config.action_model.gradient(action.action, Q[action.state], jacobian[action.state], scale);

                // Backpropagate through planner
                planner.train(jacobian);

                // propagate intent
                graph.intentGradient(intent::train);
            }

            intent.update();
        }

        void updatePolicy() {
            graph.setIntent(intent.mean());
            policy = GreedyActionModel.get().policy(planner.values());
        }
    }

    // The builder object that generated this class, used for configuration
    private Builder config;

    // The reward function mapping
    private final RewardMapping rewards;

    // The dynamics model
    private final DynamicsModel dynamics;

    // The planning graph
    private final IntentGraph graph;

    // The planning module
    private final Planner planner;

    // A buffer for backpropagating teacher data
    private final double[][] jacobian;

    // The transition data
    private final List<StateTransition> transitions;

    // The task models
    private final HashMap<String, TaskModel> tasks;

    // The current task
    private TaskModel task = null;

    private ModelBased(Representation representation, Builder config) {
        this.config = config;

        // Get reward mapping
        this.rewards = representation.rewards();

        // Initialize dynamics model
        this.dynamics = representation.newModel();
        this.dynamics.initialize(config.dynamics_optimization);

        // Build planning graph
        graph = IntentGraph.of(dynamics, rewards);

        // Initialize planner
        planner = config.planning_algorithm.planner(graph);

        // Initialize backpropagation buffer
        jacobian = new double[representation.numStates()][];

        for(int state = 0; state < representation.numStates(); ++state)
            jacobian[state] = new double[representation.numActions(state)];

        // Initialize task set
        tasks = new HashMap<>();

        // Initialize transition set
        transitions = new LinkedList<>();
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
    public void observe(StateTransition transition) { transitions.add(transition); }

    @Override
    public void integrate() {

        if(config.reinitialize) {
            for(TaskModel task : tasks.values())
                task.initialize();

            dynamics.initialize(config.dynamics_optimization);
        }

        for (int step = 0; step < config.dynamics_updates; ++step) {
            for (StateTransition transition : transitions)
                dynamics.train(transition.start, transition.action, transition.end, 1.0);

            dynamics.update();
        }

        for (int step = 0; step < config.task_updates; ++step)
            for (TaskModel task : tasks.values())
                task.update();

        for (TaskModel task : tasks.values())
            task.updatePolicy();
    }

    @Override
    public List<Visualization> visualizations() {
        List<Visualization> viz = new LinkedList<>();

        // Add dynamics image
        if(null != dynamics)
            dynamics.render().ifPresent((BufferedImage img) -> viz.add(Visualization.of(img, "dynamics")));

        for(Map.Entry<String, TaskModel> task : tasks.entrySet()) {
            final String name = task.getKey();

            rewards.render(task.getValue().intent.mean())
                    .ifPresent((BufferedImage img) -> viz.add(Visualization.of(img, "reward_" + name)));
        }

        return viz;
    }
}
