package bam;

import bam.algorithms.Expert;
import bam.util.Log;
import bam.util.RealVariable;
import bam.util.Table;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Used to conduct experiments in which
 * the algorithms is trained to perform multiple
 * tasks through demonstration.  Records
 * performance as a function of the number
 * of demonstrations, as well as the
 * number of demonstrations required
 * before the algorithms's policy is optimal
 *
 * Created by Tyler on 10/9/2017.
 */
public class MultiTaskExperiment {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Environment[] environments = null;
        private Algorithm[] algorithms = null;

        private int num_sessions = 50;
        private int max_demonstrations = 20;
        private int evaluation_episodes = 50;

        private Builder() {}

        public Builder environments(Environment... environments) {
            this.environments = environments;

            return this;
        }

        public Builder algorithms(Algorithm... algorithms) {
            this.algorithms = algorithms;

            return this;
        }

        public Builder numSessions(int num_sessions) {
            this.num_sessions = num_sessions;

            return this;
        }

        public Builder maxDemonstrations(int max_demonstrations) {
            this.max_demonstrations = max_demonstrations;

            return this;
        }

        public Builder evaluationEpisodes(int evaluation_episodes) {
            this.evaluation_episodes = evaluation_episodes;

            return this;
        }

        public MultiTaskExperiment build() {
            if(null == environments)
                throw new RuntimeException("Dumbass!!! - no environments specified");
            if(null == algorithms)
                throw new RuntimeException("Dumbass!!! - no algorithms specified");

            return new MultiTaskExperiment(this);
        }
    }

    private Environment[] environments;
    private Algorithm[] algorithms;

    private int num_sessions;
    private int max_demonstrations;
    private int evaluation_episodes;

    private ExecutorService pool;

    private MultiTaskExperiment(Builder builder) {
        this.environments = builder.environments;
        this.algorithms = builder.algorithms;
        this.num_sessions = builder.num_sessions;
        this.max_demonstrations = builder.max_demonstrations;
        this.evaluation_episodes = builder.evaluation_episodes;

        pool = Executors.newCachedThreadPool();
    }

    /*
     * Stores the results of a single training
     * session, including the expected policy
     * return after each demonstration, the
     * number of steps required for the algorithms's
     * policy to become acceptable, and the
     * final knowledge state of the algorithms.
     */
    private class Session {
        final double[] rewards;
        final Agent agent;

        Session(double[] rewards, Agent agent) {
            this.rewards = rewards;
            this.agent = agent;
        }
    }

    /*
     * Stores and processes all the sessions
     * generated under a single condition.
     */
    private class Condition {

        // The results of each session
        final List<Session> sessions;

        // The session with the best final performance
        final Session best;

        // The reward traces represented as a sample of a random variable
        final RealVariable rewards;

        // The name of the condition
        final String name;

        Condition(List<Session> sessions, String name) {
            this.sessions = sessions;
            this.name = name;

            rewards = RealVariable.vector(max_demonstrations);

            // Process sessions
            Session best_session = null;
            double max = -Double.MAX_VALUE;

            for(Session session : sessions) {
                rewards.add(session.rewards);

                if (session.rewards[max_demonstrations - 1] > max) {
                    best_session = session;
                    max = session.rewards[max_demonstrations - 1];
                }
            }

            best = best_session;
        }
    }

    private Session session(Environment environment,
                            Map<String, Expert> experts,
                            Algorithm algorithm) throws Exception {

        // Get random number generator
        Random random = ThreadLocalRandom.current();

        // Initialize algorithms
        Agent agent = algorithm.agent(environment.representation());

        // Get environment dynamics
        Dynamics dynamics = environment.dynamics();

        // Initialize reward trace
        double[] rewards = new double[max_demonstrations];
        Arrays.fill(rewards, 0.0);

        // Generate Demonstrations
        for(int demonstration = 0; demonstration < max_demonstrations; ++demonstration) {

            // One demonstration for each task
            for(Task task : environment.tasks()) {

                // Get the agent for the current task
                Expert expert = experts.get(task.name());

                // Tell the agent what the current task is
                agent.task(task.name());

                // Get initial state
                int state = task.initial(ThreadLocalRandom.current());

                // Generate trajectory
                for(int step = 0; step < environment.dynamics().depth(); ++step) {

                    // Get next action
                    int action = expert.action(state, ThreadLocalRandom.current());
                    agent.observe(TeacherAction.of(state, action));

                    // Update state
                    int next_state = dynamics.transition(state, action, ThreadLocalRandom.current());
                    agent.observe(StateTransition.of(state, action, next_state));
                    state = next_state;
                }
            }

            // Integrate the new demonstrations
            agent.integrate();

            // Evaluate policy
            for(Task task : environment.tasks()) {
                agent.task(task.name());

                for(int episode = 0; episode < evaluation_episodes; ++episode)
                    rewards[demonstration] += dynamics
                            .simulate(agent, task, task.initial(random), dynamics.depth(), random);
            }

            rewards[demonstration] /= evaluation_episodes;
        }

        return this.new Session(rewards, agent);
    }

    private Condition condition(Environment environment,
                                Map<String, Expert> experts,
                                Algorithm algorithm,
                                Log log) throws Exception {
        log.write("starting condition, environment: " + environment.name() + ", algorithm: " + algorithm.name());

        // Launch sessions
        List<Future<Session>> threads = new ArrayList<>();

        for(int session = 0; session < num_sessions; ++session)
            threads.add(pool.submit(() -> session(environment, experts, algorithm)));

        // Join sessions
        LinkedList<Session> sessions = new LinkedList<>();

        for (int session = 0; session < num_sessions; ++session) {
            sessions.add(threads.get(session).get());
            log.write("completed session " + session + ", environment: "
                    + environment.name() + ", algorithm: " + algorithm.name());
        }

        log.write("completed condition, environment: " + environment.name() + ", algorithm: " + algorithm.name());

        // Return results
        return this.new Condition(sessions, algorithm.name());
    }

    private int experiment(Environment environment, File folder, Log log) throws Exception {

        // Build experts
        Map<String, Expert> experts = new HashMap<>();

        for(Task task : environment.tasks())
            experts.put(task.name(), Expert.with(environment.dynamics(), task));

        // Compute expert performance
        Dynamics dynamics = environment.dynamics();
        double expert_performance = 0.0;

        for(Task task : environment.tasks())
            for(int episode = 0; episode < evaluation_episodes; ++episode)
                expert_performance += dynamics.simulate(experts.get(task.name()), task,
                        task.initial(ThreadLocalRandom.current()), dynamics.depth(), ThreadLocalRandom.current());

        expert_performance /= evaluation_episodes;

        // Launch conditions
        List<Future<Condition>> threads = new ArrayList<>();

        for(Algorithm algorithm : algorithms)
            threads.add(pool.submit(() -> condition(environment, experts, algorithm, log)));

        // Join conditions
        List<Condition> conditions = new ArrayList<>();

        for(Future<Condition> thread : threads)
            conditions.add(thread.get());

        // Save session data
        List<String> columns = new LinkedList<>();
        columns.add("condition");

        for(int demo = 1; demo <= max_demonstrations; ++demo)
            columns.add("d" + demo);

        Table sessions = Table.create("sessions", columns);

        for(Condition condition : conditions)
            for(Session session : condition.sessions)
                sessions.newRow()
                        .add(condition.name)
                        .add(session.rewards);

        sessions.csv(folder);

        // Save average rewards
        columns = new LinkedList<>();
        columns.add("demonstration");
        columns.add("expert");

        for(Condition condition : conditions) {
            columns.add(condition.name);
            columns.add(condition.name + "-dev");
            columns.add(condition.name + "-err");
        }

        Table rewards = Table.create("rewards", columns);

        for(int demo = 0; demo < max_demonstrations; ++demo) {
            Table.Row row = rewards.newRow().add(demo + 1);
            row.add(expert_performance);

            for(Condition condition : conditions)
                row.add(condition.rewards.mean(demo))
                        .add(condition.rewards.deviation(demo))
                        .add(condition.rewards.error(demo));
        }

        rewards.table(folder);

        // Save visualizations
        for(Condition condition : conditions)
            for(Visualization visualization : condition.best.agent.visualizations())
                ImageIO.write(visualization.image, "png",
                        new File(folder, condition.name + "_" + visualization.name + ".png"));

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
                .put("max demonstrations", max_demonstrations)
                .put("evaluation episodes", evaluation_episodes)
                .put("algorithms", algs)
                .put("environments", envs);

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
            env_root.mkdir();

            experiments.add(pool.submit(() -> experiment(environment, env_root, log)));
        }

        for(Future experiment : experiments)
            experiment.get();

        log.write("multitask experiment complete");
    }
}
