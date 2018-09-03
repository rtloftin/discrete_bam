package bam.simulation;

import bam.algorithms.*;
import bam.algorithms.feedback.FeedbackModel;
import bam.domains.Environment;
import bam.domains.Task;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.*;

public class MultiTaskCombinedExperiment {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Environment[] environments = null;
        private Algorithm[] algorithms = null;

        private FeedbackModel feedback_model = null;

        private int num_sessions = 50;
        private int num_episodes = 20;
        private int evaluation_episodes = 50;

        private boolean final_noop = true;

        private Builder() {}

        public Builder environments(Environment... environments) {
            this.environments = environments;

            return this;
        }

        public Builder algorithms(Algorithm... algorithms) {
            this.algorithms = algorithms;

            return this;
        }

        public Builder feedbackModel(FeedbackModel feedback_model) {
            this.feedback_model = feedback_model;

            return this;
        }

        public Builder numSessions(int num_sessions) {
            this.num_sessions = num_sessions;

            return this;
        }

        public Builder numEpisodes(int num_episodes) {
            this.num_episodes = num_episodes;

            return this;
        }

        public Builder evaluationEpisodes(int evaluation_episodes) {
            this.evaluation_episodes = evaluation_episodes;

            return this;
        }

        public Builder finalNoop(boolean final_noop) {
            this.final_noop = final_noop;

            return this;
        }

        public MultiTaskCombinedExperiment build() {
            if(null == environments)
                throw new RuntimeException("Dumbass!!! - no environments specified");
            if(null == algorithms)
                throw new RuntimeException("Dumbass!!! - no algorithms specified");
            if(null == feedback_model)
                throw new RuntimeException("Dumbass!!! - no algorithms specified");

            return new MultiTaskCombinedExperiment(this);
        }
    }

    private Environment[] environments;
    private Algorithm[] algorithms;

    private final FeedbackModel feedback_model;

    private int num_sessions;
    private int num_episodes;
    private int evaluation_episodes;

    private boolean final_noop;

    private ExecutorService pool;

    private MultiTaskCombinedExperiment(Builder builder) {
        this.environments = builder.environments;
        this.algorithms = builder.algorithms;
        this.feedback_model = builder.feedback_model;
        this.num_sessions = builder.num_sessions;
        this.num_episodes = builder.num_episodes;
        this.evaluation_episodes = builder.evaluation_episodes;
        this.final_noop = builder.final_noop;

        pool = Executors.newCachedThreadPool();
    }

    private Session session(Environment environment,
                            Map<String, ExpertPolicy> experts,
                            Algorithm algorithm) {

        // Get random number generator
        Random random = ThreadLocalRandom.current();

        // Initialize algorithms
        Agent agent = algorithm.agent(environment.representation());

        // Get environment dynamics
        Dynamics dynamics = environment.dynamics();

        // Initialize session
        // Session session = Session.with(agent);
        Session session = Session.get();

        // Generate data
        for(int demonstration = 0; demonstration < num_episodes; ++demonstration) {

            // Generate demonstration data
            for(Task task : environment.tasks()) {

                // Get the agent for the current task
                ExpertPolicy expert = experts.get(task.name());

                // Tell the agent what the current task is
                agent.task(task.name());

                // Get initial state
                int state = task.initial(random);

                // Generate trajectory
                for(int step = 0; step < environment.dynamics().depth(); ++step) {

                    // Get next action
                    int action = expert.action(state, random);
                    agent.observe(TeacherAction.of(state, action));

                    // Update state
                    int next_state = dynamics.transition(state, action, random);
                    agent.observe(StateTransition.of(state, action, next_state));
                    state = next_state;

                    // Check if goal state
                    if(task.reward(state) > 0.0) {

                        // Do the final noop if necessary
                        if(final_noop) {
                            action = expert.action(state, random);
                            agent.observe(TeacherAction.of(state, action));

                            next_state = dynamics.transition(state, action, random);
                            agent.observe(StateTransition.of(state, action, next_state));
                        }

                        // End demonstration early
                        break;
                    }
                }
            }

            // Integrate the demonstrations
            agent.integrate();

            // Generate feedback data
            for(Task task : environment.tasks()) {

                // Get the agent for the current task
                ExpertPolicy expert = experts.get(task.name());

                // Tell the agent what the current task is
                agent.task(task.name());

                // Get initial state
                int state = task.initial(random);

                // Generate trajectory
                for(int step = 0; step < environment.dynamics().depth(); ++step) {

                    // Get next action
                    int action = agent.action(state, random);
                    double feedback = feedback_model.feedback(action, expert.values(state), random);

                    agent.observe(TeacherFeedback.of(state, action, feedback));

                    // Update state
                    int next_state = dynamics.transition(state, action, random);
                    agent.observe(StateTransition.of(state, action, next_state));
                    state = next_state;
                }
            }

            // Integrate the feedback
            agent.integrate();

            // Evaluate policy
            double performance = 0.0;

            for(Task task : environment.tasks()) {
                agent.task(task.name());

                for(int episode = 0; episode < evaluation_episodes; ++episode)
                    performance += dynamics
                            .simulate(agent, task, task.initial(random), dynamics.depth(), random);
            }

            session.episode(performance / evaluation_episodes);
        }

        // Return results
        return session;
    }

    private Condition condition(Environment environment,
                                Map<String, ExpertPolicy> experts,
                                Algorithm algorithm,
                                Log log) throws Exception {

        // Print initial message
        log.write("starting condition, environment: " + environment.name() + ", algorithm: " + algorithm.name());

        // Initialize condition
        Condition condition = Condition.with(algorithm.name());

        // Launch sessions
        List<Future<Session>> threads = new ArrayList<>();

        for(int session = 0; session < num_sessions; ++session) {
            // threads.add(pool.submit(() -> session(environment, experts, algorithm)));
            threads.add(CompletableFuture.completedFuture(session(environment, experts, algorithm)));
        }

        // Join sessions
        for (int session = 0; session < num_sessions; ++session)
            condition.add(threads.get(session).get());

        // Print completion message
        log.write("completed condition, environment: " + environment.name() + ", algorithm: " + algorithm.name());

        // Return results
        return condition;
    }

    private int experiment(Environment environment, File folder, Log log) throws Exception {

        // Build experts
        Map<String, ExpertPolicy> experts = new HashMap<>();

        for(Task task : environment.tasks())
            experts.put(task.name(), ExpertPolicy.with(environment.dynamics(), task));

        // Compute expert performance
        Dynamics dynamics = environment.dynamics();
        double expert_performance = 0.0;

        for(Task task : environment.tasks())
            for(int episode = 0; episode < evaluation_episodes; ++episode)
                expert_performance += dynamics.simulate(experts.get(task.name()), task,
                        task.initial(ThreadLocalRandom.current()), dynamics.depth(), ThreadLocalRandom.current());

        expert_performance /= evaluation_episodes;

        // Compute baseline performance
        BaselinePolicy baseline = BaselinePolicy.with(dynamics);
        double baseline_performance = 0.0;

        for(Task task : environment.tasks())
            for(int episode = 0; episode < evaluation_episodes; ++episode)
                baseline_performance += dynamics.simulate(baseline, task,
                        task.initial(ThreadLocalRandom.current()), dynamics.depth(), ThreadLocalRandom.current());

        baseline_performance /= evaluation_episodes;

        // Launch conditions
        List<Future<Condition>> threads = new ArrayList<>();

        for(Algorithm algorithm : algorithms)
            threads.add(pool.submit(() -> condition(environment, experts, algorithm, log)));

        // Join conditions
        List<Condition> conditions = new ArrayList<>();

        for(Future<Condition> thread : threads)
            conditions.add(thread.get());

        // Record data for this experiment
        Condition.record(folder, expert_performance, baseline_performance, conditions.toArray(new Condition[0]));

        return 0;
    }

    /**
     * Runs the experiment for all algorithms
     * in all environments, and saves the results
     * in the specified directory.
     *
     * @param root the directory in which to store the data, initially assumed to be empty
     */
    public void run(File root) throws Exception {

        // Initialize data directory
        root.mkdirs();

        // Initialize log
        Log log = Log.combined(new File(root, "log"));
        log.write("started multitask experiment");

        // Save configuration data
        JSONArray algs = new JSONArray();

        for(Algorithm algorithm : algorithms)
            algs.put(algorithm.serialize());

        JSONArray envs = new JSONArray();

        for(Environment environment : environments)
            envs.put(environment.serialize());

        JSONObject json = new JSONObject()
                .put("name", "multitask experiment")
                .put("class", this.getClass().getCanonicalName())
                .put("num sessions", num_sessions)
                .put("num episodes", num_episodes)
                .put("evaluation episodes", evaluation_episodes)
                .put("final noop", final_noop)
                .put("algorithms", algs)
                .put("environments", envs)
                .put("feedback model", feedback_model.serialize());

        PrintStream config = new PrintStream(new File(root, "config"));
        config.print(json.toString(4));
        config.close();

        // Render environments
        for(Environment environment : environments)
            environment.render().ifPresent((BufferedImage img) -> {
                try {
                    ImageIO.write(img, "png", new File(root, environment.name() + ".png"));
                } catch(Exception e) { log.write(e.getMessage()); }
            });

        // Run experiment
        List<Future> experiments = new LinkedList<>();

        for(Environment environment : environments) {
            File env_root = new File(root, environment.name());
            env_root.mkdirs();

            experiments.add(pool.submit(() -> experiment(environment, env_root, log)));
        }

        for(Future experiment : experiments)
            experiment.get();

        log.write("multitask experiment complete");
    }
}
