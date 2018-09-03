package bam.simulation;

import bam.algorithms.Agent;
import bam.algorithms.Visualization;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a set of sessions generated
 * with a single learning algorithm
 * under a specific set of
 * experimental conditions.
 */
public class Condition implements Iterable<Session> {

    private final String name;

    private final List<Session> sessions;
    private RealVariable performance_episodes;

    private Condition(String name) {
        this.name = name;

        sessions = new ArrayList<>();
        performance_episodes = RealVariable.get();
    }

    public static Condition with(String name) {
        return new Condition(name);
    }

    public String name() {
        return name;
    }

    public void add(Session session) {
        sessions.add(session);

        double[] performance = new double[session.episodes()];

        for(int episode = 0; episode < session.episodes(); ++episode)
            performance[episode] = session.performance(episode);

        performance_episodes.add(performance);
    }

    public int episodes() {
        return performance_episodes.size();
    }

    public double mean(int index) {
        return performance_episodes.mean(index);
    }

    public double deviation(int index) {
        return performance_episodes.deviation(index);
    }

    public double error(int index) {
        return performance_episodes.error(index);
    }

    public JSONObject serialize() throws JSONException {
        JSONArray json_sessions = new JSONArray();

        for(Session session : sessions)
            json_sessions.put(session.serialize());

        return new JSONObject()
                .put("name", name)
                .put("sessions", json_sessions);
    }

    @Override
    public Iterator<Session> iterator() {
        return sessions.iterator();
    }

    public static void record(File folder, double expert, double baseline, Condition... conditions) throws IOException, JSONException {

        // Save raw session data as JSON
        JSONArray json_conditions = new JSONArray();

        for(Condition condition : conditions)
            json_conditions.put(condition.serialize());

        PrintStream data = new PrintStream(new File(folder, "data"));
        data.print(json_conditions.toString(2));
        data.close();

        // Plot performance by episodes
        LinkedList<String> columns = new LinkedList<>();
        columns.add("Episode");
        columns.add("Baseline");
        columns.add("Expert");

        for(Condition condition : conditions) {
            columns.add(condition.name);
            columns.add(condition.name + "-dev");
            columns.add(condition.name + "-err");
        }

        Table performance = Table.create("performance_episode", columns);

        int max_episodes = 0;

        for(Condition condition : conditions)
            if (condition.episodes() > max_episodes)
                max_episodes = condition.episodes();

        for(int episode = 0; episode < max_episodes; ++episode) {
            Table.Row row = performance.newRow().add(episode + 1);
            row.add(baseline);
            row.add(expert);

            for(Condition condition : conditions) {
                row.add(condition.mean(episode))
                        .add(condition.deviation(episode))
                        .add(condition.error(episode));
            }
        }

        performance.table(folder);

        // Generate columns for performance comparison
        columns = new LinkedList<>();
        columns.add("\"algorithm\"");
        columns.add("\"10%\"");
        columns.add("\"20%\"");
        columns.add("\"30%\"");
        columns.add("\"40%\"");
        columns.add("\"50%\"");
        columns.add("\"60%\"");
        columns.add("\"70%\"");
        columns.add("\"80%\"");
        columns.add("\"90%\"");

        // Generate performance thresholds
        double[] thresholds = new double[9];
        thresholds[0] = 0.1 * expert;
        thresholds[1] = 0.2 * expert;
        thresholds[2] = 0.3 * expert;
        thresholds[3] = 0.4 * expert;
        thresholds[4] = 0.5 * expert;
        thresholds[5] = 0.6 * expert;
        thresholds[6] = 0.7 * expert;
        thresholds[7] = 0.8 * expert;
        thresholds[8] = 0.9 * expert;

        // Save data for performance comparisons -- by episodes
        Table effort = Table.create("effort_episodes", columns);

        for(Condition condition : conditions) {
            for (Session session : condition) {
                Table.Row row = effort.newRow().add(condition.name);

                for (int level = 0; level < thresholds.length; ++level) {
                    int episode = 0;

                    while (episode < session.episodes() && session.performance(episode) < thresholds[level])
                        ++episode;

                    if (episode < session.episodes()) {
                        row.add(episode + 1);
                    } else {
                        row.add("NA");
                    }
                }
            }
        }

        effort.csv(folder);

        // Save data for success comparisons -- a little more complex
        Table success = Table.create("success", columns);

        for(Condition condition : conditions) {
            for (Session session : condition) {
                Table.Row row = success.newRow().add(condition.name);

                for (int level = 0; level < thresholds.length; ++level) {
                    if(session.performance() >= thresholds[level]) {
                        row.add("TRUE");
                    } else {
                        row.add("FALSE");
                    }
                }
            }
        }

        success.csv(folder);

        // Generate visualizations -- we can't store all of the agent objects

        /*
        for(Condition condition : conditions) {
            double max = -Double.MAX_VALUE;
            Agent best = null;

            for(Session session : condition)
                if(session.performance() >= max) {
                    max = session.performance();
                    best = session.agent();
                }

            for(Visualization visualization : best.visualizations())
                ImageIO.write(visualization.image, "png",
                        new File(folder, condition.name + "_" + visualization.name + ".png"));
        }*/
    }
}
