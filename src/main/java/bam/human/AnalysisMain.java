package bam.human;

import bam.domains.Environment;
import bam.domains.farm_world.FarmWorlds;
import bam.domains.grid_world.GridWorlds;
import bam.human.analysis.*;
import org.json.JSONException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is the entry point for the offline
 * analysis of data collected during a user study.
 * This will select a study directory, and will
 * compute the summary statistics needed for
 * our analysis.
 */
public class AnalysisMain {

    public static void main(String[] args) throws IOException, JSONException {

        // Load data
        Path root = Paths.get("C:\\Users\\Tyler\\Desktop\\users_4_4");
        DataSet data = DataSet.load(root);

        // Print number of users
        System.out.println("total participants: " + data.participants());

        // Filter out incomplete and tutorial sessions
        Sessions all_complete = data.sessions(UserFilter.all(), SessionFilter.tutorial(), SessionFilter.complete());

        System.out.println("Complete experimental sessions: " + all_complete.records().size());

        // Divide sessions by environment
        Sessions two_rooms = all_complete.filter(SessionFilter.environment("two-rooms"));
        Sessions doors = all_complete.filter(SessionFilter.environment("doors"));
        Sessions two_fields = all_complete.filter(SessionFilter.environment("two-fields"));
        Sessions six_fields = all_complete.filter(SessionFilter.environment("six-fields"));

        System.out.println("Two Rooms: " + two_rooms.records().size() + " sessions");
        System.out.println("Doors: " + doors.records().size() + " sessions");
        System.out.println("Two Fields: " + two_fields.records().size() + " sessions");
        System.out.println("Six Rooms: " + six_fields.records().size() + " sessions");


        Path results_root = root.resolve("analysis");
        process(two_rooms, GridWorlds.twoRooms(), results_root.resolve("two-rooms"));
        process(doors, GridWorlds.doors(), results_root.resolve("doors"));
        process(two_fields, FarmWorlds.twoFields(), results_root.resolve("two-fields"));
        process(six_fields, FarmWorlds.sixFields(), results_root.resolve("six-fields"));

        /*
         * The first step in the data analysis would be to filter out all the sessions we aren't
         * interested in, then we need to analyze the policies learned during each of these sessions
         * to determine their accuracy and their overall performance, which could be time consuming.
         *
         * We also need to be able to convert policies into time series as a function of steps taken
         * and demonstrations given.  We also need to be able to ask if the agent's behavior during
         * a session reached a given performance threshold, and how long it took to reach that, on
         * multiple time scales (actions, wall clock).
         */
    }

    private static void process(Sessions sessions, Environment environment, Path directory) {

        System.out.println("\n" + environment.name() + "\n");

        Evaluation<Double> mean = Evaluation.mean(environment, 100);

        Sequences<Double> bam = Sequences.of(sessions.filter(SessionFilter.algorithm("BAM")), mean);
        Sequences<Double> model_based = Sequences.of(sessions.filter(SessionFilter.algorithm("Model-Based")), mean);
        Sequences<Double> cloning = Sequences.of(sessions.filter(SessionFilter.algorithm("Cloning")), mean);

        TimeScale actions = TimeScale.actions();
        TimeSeries<Double> bam_actions = TimeSeries.of(bam, actions);
        TimeSeries<Double> model_based_actions = TimeSeries.of(model_based, actions);
        TimeSeries<Double> cloning_actions = TimeSeries.of(cloning, actions);

        double bam_mean = 0.0;
        for(Double value : bam_actions.performance())
            bam_mean += value;

        bam_mean /= bam_actions.performance().size();
        System.out.println("BAM, mean performance: " + bam_mean);

        double model_based_mean = 0.0;
        for(Double value : model_based_actions.performance())
            model_based_mean += value;

        model_based_mean /= model_based_actions.performance().size();
        System.out.println("Model Based, mean performance: " + model_based_mean);

        double cloning_mean = 0.0;
        for(Double value : cloning_actions.performance())
            cloning_mean += value;

        cloning_mean /= cloning_actions.performance().size();
        System.out.println("Cloning, mean performance: " + cloning_mean);

        // System.out.println("BAM: " + bam.size() + " sessions, " + bam_actions.size() + " actions");
        // System.out.println("Model Based: " + model_based.size() + " sessions, " + model_based_actions.size() + " actions");
        // System.out.println("Cloning: " + cloning.size() + " sessions, " + cloning_actions.size() + " actions");
    }
}
