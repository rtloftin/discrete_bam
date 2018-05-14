package bam.human.analysis;

import bam.algorithms.Agent;
import bam.algorithms.Visualization;
import bam.simulation.RealVariable;
import bam.simulation.Session;
import bam.simulation.Table;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
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
public class Condition {

    private final String name;
    private final TimeSeries<Performance> series;

    private RealVariable ratios;
    private RealVariable trained_ratios;

    private Condition(String name, TimeSeries<Performance> series) {
        this.name = name;
        this.series = series;

        ratios = RealVariable.get();
        trained_ratios = RealVariable.get();

        for(List<Performance> session : series) {
            double[] ratio = new double[session.size()];
            double[] trained_ratio = new double[session.size()];

            for(int step = 0; step < session.size(); ++step) {
                Performance performance = session.get(step);

                ratio[step] = performance.ratio();
                trained_ratio[step] = performance.trainedRatio();
            }

            ratios.add(ratio);
            trained_ratios.add(trained_ratio);
        }
    }

    public static Condition of(String name, TimeSeries<Performance> series) {
        return new Condition(name, series);
    }

    public static Condition of(String name, List<AnnotatedSession<Performance>> sessions, TimeScale time_scale) {
        return new Condition(name, TimeSeries.of(sessions, time_scale));
    }

    public String name() {
        return name;
    }

    public TimeSeries<Performance> series() {
        return series;
    }

    public int steps() {
        return series.steps();
    }

    public double ratioMean(int index) {
        return ratios.mean(index);
    }

    public double ratioError(int index) {
        return ratios.error(index);
    }

    public double trainedRatioMean(int index) {
        return trained_ratios.mean(index);
    }

    public double trainedRatioError(int index) {
        return trained_ratios.error(index);
    }

    public static void record(Path directory, TimeScale timescale, Condition... conditions) throws IOException {

        // Plot performance by episodes
        LinkedList<String> columns = new LinkedList<>();
        columns.add("Time");

        for(Condition condition : conditions) {
            columns.add(condition.name);
            columns.add(condition.name + "-err");
        }

        Table performance_all = Table.create("performance_all", columns);
        Table performance_trained = Table.create("performance_trained", columns);

        int max_timesteps = 0;

        for(Condition condition : conditions)
            if (condition.steps() > max_timesteps)
                max_timesteps = condition.steps();

        for(int step = 0; step < max_timesteps; ++step) {
            int time = timescale.time(step);

            Table.Row row_all = performance_all.newRow().add(time);
            Table.Row row_trained = performance_trained.newRow().add(time);

            for(Condition condition : conditions) {
                row_all.add(condition.ratioMean(step))
                        .add(condition.ratioError(step));

                row_trained.add(condition.trainedRatioMean(step)).
                        add(condition.trainedRatioError(step));
            }
        }

        performance_all.table(directory.toFile());
        performance_trained.table(directory.toFile());

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
        thresholds[0] = 0.1;
        thresholds[1] = 0.2;
        thresholds[2] = 0.3;
        thresholds[3] = 0.4;
        thresholds[4] = 0.5;
        thresholds[5] = 0.6;
        thresholds[6] = 0.7;
        thresholds[7] = 0.8;
        thresholds[8] = 0.9;

        // Save data for performance comparisons -- by time step
        Table effort_all = Table.create("effort_all", columns);
        Table effort_trained = Table.create("effort_trained", columns);

        for(Condition condition : conditions) {
            for (List<Performance> session : condition.series()) {
                Table.Row row_all = effort_all.newRow().add(condition.name);
                Table.Row row_trained = effort_trained.newRow().add(condition.name);

                // Overall performance
                for (int level = 0; level < thresholds.length; ++level) {
                    int step = 0;

                    while (step < session.size() && session.get(step).ratio() < thresholds[level])
                        ++step;

                    if (step < session.size()) {
                        row_all.add(step + 1);
                    } else {
                        row_all.add("NA");
                    }
                }

                // Trained task performance
                for (int level = 0; level < thresholds.length; ++level) {
                    int step = 0;

                    while (step < session.size() && session.get(step).trainedRatio() < thresholds[level])
                        ++step;

                    if (step < session.size()) {
                        row_trained.add(step + 1);
                    } else {
                        row_trained.add("NA");
                    }
                }
            }
        }

        effort_all.csv(directory.toFile());
        effort_trained.csv(directory.toFile());

        // Save data for success comparisons -- a little more complex
        Table success_all = Table.create("success_all", columns);
        Table success_trained = Table.create("success_trained", columns);

        for(Condition condition : conditions) {
            for (List<Performance> session : condition.series()) {
                Table.Row row_all = success_all.newRow().add(condition.name);
                Table.Row row_trained = success_trained.newRow().add(condition.name);

                int size = session.size();

                // Overall success
                for (int level = 0; level < thresholds.length; ++level) {
                    if(0 != size && session.get(size- 1).ratio() >= thresholds[level]) {
                        row_all.add("TRUE");
                    } else {
                        row_all.add("FALSE");
                    }
                }

                // Trained task success
                for (int level = 0; level < thresholds.length; ++level) {
                    if(0 != size && session.get(size - 1).trainedRatio() >= thresholds[level]) {
                        row_trained.add("TRUE");
                    } else {
                        row_trained.add("FALSE");
                    }
                }
            }
        }

        success_all.csv(directory.toFile());
        success_trained.csv(directory.toFile());
    }
}
