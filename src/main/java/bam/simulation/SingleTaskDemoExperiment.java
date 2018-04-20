package bam.simulation;

import bam.algorithms.*;
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

/**
 * Used to conduct experiments in which
 * the algorithms is trained to perform a single
 * task through demonstration.  Records
 * performance as a function of the number
 * of demonstrations, as well as the
 * number of demonstrations required
 * before the algorithms's policy is optimal
 *
 * Created by Tyler on 10/9/2017.
 */
public class SingleTaskDemoExperiment {

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

        public SingleTaskDemoExperiment build() {
            if(null == environments)
                throw new RuntimeException("Dumbass!!! - no environments specified");
            if(null == algorithms)
                throw new RuntimeException("Dumbass!!! - no algorithms specified");

            return new SingleTaskDemoExperiment(this);
        }
    }

    private Environment[] environments;
    private Algorithm[] algorithms;

    private int num_sessions;
    private int max_demonstrations;
    private int evaluation_episodes;

    private ExecutorService pool;

    private SingleTaskDemoExperiment(Builder builder) {
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
                            Task task,
                            Expert expert,
                            Algorithm algorithm) {

        // Get random number generator
        Random random = ThreadLocalRandom.current();

        // Initialize learning agent
        Agent agent = algorithm.agent(environment.representation());
        agent.task(task.name());

        // Get environment dynamics
        Dynamics dynamics = environment.dynamics();

        // Initialize reward trace
        double[] rewards = new double[max_demonstrations];
        Arrays.fill(rewards, 0.0);

        // Generate Demonstrations
        for(int demonstration = 0; demonstration < max_demonstrations; ++demonstration) {

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
            }

            // Integrate the new demonstration
            agent.integrate();

            // Evaluate policy
            for(int episode = 0; episode < evaluation_episodes; ++episode)
                rewards[demonstration] += dynamics
                        .simulate(agent, task, task.initial(random), dynamics.depth(), random);

            rewards[demonstration] /= evaluation_episodes;
        }

        return this.new Session(rewards, agent);
    }

    private Condition condition(Environment environment,
                                Task task,
                                Expert expert,
                                Algorithm algorithm,
                                Log log) throws Exception {
        log.write("starting condition, environment: " + environment.name()
                + ", task: " + task.name() + ", algorithm: " + algorithm.name());

        // Launch sessions
        List<Future<Session>> threads = new ArrayList<>();

        for(int session = 0; session < num_sessions; ++session)
            threads.add(pool.submit(() -> session(environment, task, expert, algorithm)));

        // Join sessions
        List<Session> sessions = new ArrayList<>();

        for (int session = 0; session < num_sessions; ++session) {
            sessions.add(threads.get(session).get());
            log.write("completed session " + session+ ", environment: "
                    + environment.name() + ", task: " + task.name() + ", algorithm: " + algorithm.name());
        }

        log.write("completed condition, environment: " + environment.name()
                + ", task: " + task.name() + ", algorithm: " + algorithm.name());

        // Return results
        return this.new Condition(sessions, algorithm.name());
    }

    private int experiment(Environment environment, Task task, File folder, Log log) throws Exception {

        // Build expert
        Expert expert = Expert.with(environment.dynamics(), task);

        // Compute expert performance
        Dynamics dynamics = environment.dynamics();
        double expert_performance = 0.0;

        for(int episode = 0; episode < evaluation_episodes; ++episode)
            expert_performance += dynamics.simulate(expert, task, task.initial(ThreadLocalRandom.current()),
                    dynamics.depth(), ThreadLocalRandom.current());

        expert_performance /= evaluation_episodes;

        // Compute baseline performance
        Baseline baseline = Baseline.with(dynamics);
        double baseline_performance = 0.0;

        for(int episode = 0; episode < evaluation_episodes; ++episode)
            baseline_performance += dynamics.simulate(baseline, task,
                    task.initial(ThreadLocalRandom.current()), dynamics.depth(), ThreadLocalRandom.current());

        baseline_performance /= evaluation_episodes;

        // Launch conditions
        List<Future<Condition>> threads = new ArrayList<>();

        for(Algorithm algorithm : algorithms)
            threads.add(pool.submit(() -> condition(environment, task, expert, algorithm, log)));

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
        columns.add("Demonstration");
        columns.add("Baseline");
        columns.add("Expert");

        for(Condition condition : conditions) {
            columns.add(condition.name);
            columns.add(condition.name + "-dev");
            columns.add(condition.name + "-err");
        }

        Table rewards = Table.create("rewards", columns);

        for(int demo = 0; demo < max_demonstrations; ++demo) {
            Table.Row row = rewards.newRow().add(demo + 1);
            row.add(baseline_performance);
            row.add(expert_performance);

            for(Condition condition : conditions)
                row.add(condition.rewards.mean(demo))
                        .add(condition.rewards.deviation(demo))
                        .add(condition.rewards.error(demo));
        }

        rewards.table(folder);

        // Save visualizations
        for(Condition condition : conditions) {

            for (Visualization visualization : condition.best.agent.visualizations())
                ImageIO.write(visualization.image, "png",
                        new File(folder, condition.name + "_" + visualization.name + ".png"));

        }

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
        if(!root.mkdirs())
            throw new Exception("Could not create data directory");

        // Initialize log
        Log log = Log.combined(new File(root, "log"));
        log.write("started single task experiment");

        // Save configuration data
        JSONArray algs = new JSONArray();

        for(Algorithm algorithm : algorithms)
            algs.put(algorithm.serialize());

        JSONArray envs = new JSONArray();

        for(Environment environment : environments)
            envs.put(environment.serialize());

        JSONObject json = new JSONObject()
                .put("name", "single task demonstration experiment")
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
                } catch (Exception e) {
                    log.write(e.getMessage());
                }
            });

        // Run experiment
        List<Future> experiments = new LinkedList<>();

        for(Environment environment : environments) {
            File env_root = new File(root, environment.name());

            if(!env_root.mkdir())
                throw new Exception("Could not create data directory");

            for(Task task : environment.tasks()) {
                File task_root = new File(env_root, task.name());

                if(!task_root.mkdir())
                    throw new Exception("Could not create data directory");

                experiments.add(pool.submit(() -> experiment(environment, task, task_root, log)));
            }
        }

        for(Future experiment : experiments)
            experiment.get();

        log.write("single task experiment complete");
    }
}
