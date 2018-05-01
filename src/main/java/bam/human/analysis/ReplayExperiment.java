package bam.human.analysis;

import bam.algorithms.*;
import bam.domains.Environment;
import bam.domains.Task;
import bam.simulation.Log;
import bam.simulation.Table;
import bam.simulation.RealVariable;
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
public class ReplayExperiment {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Environment environment = null;
        private Algorithm[] algorithms = null;
        private SessionRecords sessions = null;

        private int evaluation_episodes = 50;

        private Builder() { }

        public Builder environment(Environment environment) {
            this.environment = environment;

            return this;
        }

        public Builder algorithms(Algorithm... algorithms) {
            this.algorithms = algorithms;

            return this;
        }

        public Builder sessions(SessionRecords sessions) {
            this.sessions = sessions;

            return this;
        }

        public Builder evaluationEpisodes(int evaluation_episodes) {
            this.evaluation_episodes = evaluation_episodes;

            return this;
        }

        public ReplayExperiment build() {
            if(null == environment)
                throw new RuntimeException("Dumbass!!! - no environment specified");
            if(null == algorithms)
                throw new RuntimeException("Dumbass!!! - no algorithms specified");
            if(null == sessions)
                throw new RuntimeException("Dumbass!!! - no user sessions specified");

            return new ReplayExperiment(this);
        }
    }

    private final Environment environment;
    private final Algorithm[] algorithms;
    private final SessionRecords sessions;

    private final int evaluation_episodes;

    private ExecutorService pool;

    private ReplayExperiment(Builder builder) {
        this.environment = builder.environment;
        this.algorithms = builder.algorithms;
        this.sessions = builder.sessions;

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

        // The maximum number of episodes
        final int max;

        // The reward traces represented as a sample of a random variable
        final RealVariable rewards;

        // The name of the condition
        final String name;

        Condition(List<Session> sessions, String name) {
            this.sessions = sessions;
            this.name = name;

            rewards = RealVariable.get();

            // Process sessions
            Session best_session = null;
            double max_value = -Double.MAX_VALUE;
            int max_episodes = 0;

            for(Session session : sessions) {
                rewards.add(session.rewards);

                if (session.rewards[session.rewards.length - 1] > max_value) {
                    best_session = session;
                    max_value = session.rewards[session.rewards.length - 1];
                }

                if(session.rewards.length > max_episodes)
                    max_episodes = session.rewards.length;
            }

            best = best_session;
            max = max_episodes;
        }
    }

    private Session session(Environment environment,
                            Algorithm algorithm,
                            SessionRecord record) {

        Dynamics dynamics = environment.dynamics();
        Random random = ThreadLocalRandom.current();

        // Initialize algorithms
        Agent agent = algorithm.agent(environment.representation());

        // Initialize reward trace
        ArrayList<Double> rewards = new ArrayList<>();

        // Generate Demonstrations
        for(JSONObject event : record.events) {
            String type = event.getString("type");

            if(type.equals("task")) {
                agent.task(event.getJSONObject("task").getString("name"));
            } else if(type.equals("take-action")) {
                int start = event.getJSONObject("start").getInt("state");
                int action = event.getJSONObject("action").getInt("action");
                int end = event.getJSONObject("start").getInt("state");

                if(event.getBoolean("on-task"))
                    agent.observe(TeacherAction.of(start, action));

                agent.observe(StateTransition.of(start, action, end));
            } else if(type.equals("get-action")) {
                int start = event.getJSONObject("start").getInt("state");
                int action = event.getJSONObject("action").getInt("action");
                int end = event.getJSONObject("start").getInt("state");

                agent.observe(StateTransition.of(start, action, end));
            } else if(type.equals("feedback")) {
                int state = event.getJSONObject("state").getInt("state");
                int action = event.getJSONObject("action").getInt("action");

                double value = (event.getJSONObject("feedback").getString("type").equals("reward")) ? 1.0 : -1.0;

                agent.observe(TeacherFeedback.of(state, action, value));
            } else if(type.equals("update")) {

                // Integrate the new data
                agent.integrate();

                // Evaluate policy
                double reward = 0.0;

                for(Task task : environment.tasks()) {
                    agent.task(task.name());

                    for(int episode = 0; episode < evaluation_episodes; ++episode)
                        reward += dynamics.simulate(agent, task, task.initial(random), dynamics.depth(), random);
                }

                rewards.add(reward / evaluation_episodes);
            }
        }

        return this.new Session(rewards.stream().mapToDouble(r -> r).toArray(), agent);
    }

    private Condition condition(Environment environment,
                                Algorithm algorithm,
                                Log log) throws Exception {
        log.write("starting condition, environment: " + environment.name() + ", algorithm: " + algorithm.name());

        // Launch sessions
        List<Future<Session>> threads = new ArrayList<>();

        for(SessionRecord record : sessions)
            threads.add(pool.submit(() -> session(environment, algorithm, record)));

        // Join sessions
        LinkedList<Session> sessions = new LinkedList<>();

        for (Future<Session> thread : threads) {
            sessions.add(thread.get());
            log.write("completed session, algorithm: " + algorithm.name());
        }

        log.write("completed condition, environment: " + environment.name() + ", algorithm: " + algorithm.name());

        // Return results
        return this.new Condition(sessions, algorithm.name());
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
            threads.add(pool.submit(() -> condition(environment, algorithm, log)));

        // Join conditions
        List<Condition> conditions = new ArrayList<>();

        for(Future<Condition> thread : threads)
            conditions.add(thread.get());

        // Save average rewards
        List<String> columns = new LinkedList<>();
        columns.add("Demonstration");
        columns.add("Baseline");
        columns.add("Expert");

        for(Condition condition : conditions) {
            columns.add(condition.name);
            columns.add(condition.name + "-dev");
            columns.add(condition.name + "-err");
        }

        // Get the maximum number of episodes
        int max_episodes = 0;

        for(Condition condition : conditions)
            if(condition.max > max_episodes)
                max_episodes = condition.max;

        Table rewards = Table.create("rewards", columns);

        for(int episode = 0; episode < max_episodes; ++episode){
            Table.Row row = rewards.newRow().add(episode + 1);
            row.add(baseline_performance);
            row.add(expert_performance);

            for(Condition condition : conditions) {
                if(condition.rewards.size() > episode) {
                    row.add(condition.rewards.mean(episode),
                            condition.rewards.deviation(episode), condition.rewards.error(episode));
                } else {
                    row.add(0.0, 0.0, 0.0);
                }
            }
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

        JSONObject json = new JSONObject()
                .put("name", "replay experiment")
                .put("class", this.getClass().getCanonicalName())
                .put("num sessions", sessions.size())
                .put("evaluation episodes", evaluation_episodes)
                .put("algorithms", algs)
                .put("environments", environment.serialize());

        PrintStream config = new PrintStream(new File(root, "config"));
        config.print(json.toString(4));
        config.close();

        // Render environment
        environment.render().ifPresent((BufferedImage img) -> {
            try {
                ImageIO.write(img, "png", new File(root, environment.name() + ".png"));
            } catch(Exception e) { log.write(e.getMessage()); }
        });

        // Run experiment
        File env_root = new File(root, environment.name());
        env_root.mkdirs();

        pool.submit(() -> experiment(environment, env_root, log)).get();

        log.write("multitask experiment complete");
    }
}
