package bam.human;

import bam.domains.Environment;
import bam.domains.farm_world.FarmWorlds;
import bam.domains.grid_world.GridWorlds;
import bam.human.analysis.*;
import org.json.JSONException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        // Path root = Paths.get("C:\\Users\\Tyler\\Desktop\\users_4_12");
        Path root = Paths.get("C:\\Users\\Tyler\\Desktop\\test_users_4_27\\web");
        DataSet data = DataSet.load(root, EventDecoder.compressedJSON());

        // Print number of users
        System.out.println("total participants: " + data.participants().size());

        // Filter out incomplete and tutorial sessions
        SessionRecords all_complete = data.participants().filter(SessionFilter.tutorial(), SessionFilter.complete());

        System.out.println("Complete experimental sessions: " + all_complete.size());

        // Divide sessions by environment
        SessionRecords two_rooms = all_complete.filter(SessionFilter.environment("two-rooms"));
        SessionRecords doors = all_complete.filter(SessionFilter.environment("doors"));
        SessionRecords two_fields = all_complete.filter(SessionFilter.environment("two-fields"));
        SessionRecords three_fields = all_complete.filter(SessionFilter.environment("three-fields"));

        System.out.println("Two Rooms: " + two_rooms.size() + " sessions");
        System.out.println("Doors: " + doors.size() + " sessions");
        System.out.println("Two Fields: " + two_fields.size() + " sessions");
        System.out.println("Three Rooms: " + three_fields.size() + " sessions");


        Path results_root = root.resolve("analysis");
        process(two_rooms, GridWorlds.twoRooms(), results_root.resolve("two-rooms"));
        process(doors, GridWorlds.doors(), results_root.resolve("doors"));
        process(two_fields, FarmWorlds.twoFields(), results_root.resolve("two-fields"));
        process(three_fields, FarmWorlds.threeFields(), results_root.resolve("three-fields"));

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

    private static void process(SessionRecords sessions, Environment environment, Path directory) {

        System.out.println("\n" + environment.name() + "\n");

        Performance.Evaluation evaluation = Performance.evaluation(100, environment);
        SessionStatistic<Performance> statistic = SessionStatistic.performance(evaluation);

        List<Performance> bam = statistic.of(sessions.filter(SessionFilter.algorithm("BAM")));
        List<Performance> model_based = statistic.of(sessions.filter(SessionFilter.algorithm("Model-Based")));
        List<Performance> cloning = statistic.of(sessions.filter(SessionFilter.algorithm("Cloning")));

        double bam_mean = 0.0;
        double bam_trained = 0.0;
        for(Performance performance : bam) {
            bam_mean += performance.mean();
            bam_trained += performance.trainedMean();
        }

        bam_mean /= bam.size();
        bam_trained /= bam.size();
        System.out.println("BAM, " + bam.size() + " sessions, mean: " + bam_mean + ", trained mean: " + bam_trained);

        double model_mean = 0.0;
        double model_trained = 0.0;
        for(Performance performance : model_based) {
            model_mean += performance.mean();
            model_trained += performance.trainedMean();
        }

        model_mean /= model_based.size();
        model_trained /= model_based.size();
        System.out.println("Model Based, " + model_based.size() + " sessions, mean: " + model_mean + ", trained mean: " + model_trained);

        double cloning_mean = 0.0;
        double cloning_trained = 0.0;
        for(Performance performance : cloning) {
            cloning_mean += performance.mean();
            cloning_trained += performance.trainedMean();
        }

        cloning_mean /= bam.size();
        cloning_trained /= bam.size();
        System.out.println("Cloning, " + cloning.size() + " sessions, mean: " + cloning_mean + ", trained mean: " + cloning_trained);
    }
}
