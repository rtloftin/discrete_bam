package bam.human;

import bam.algorithms.Dynamics;
import bam.algorithms.ExpertPolicy;
import bam.algorithms.Policy;
import bam.domains.Environment;
import bam.domains.Task;
import bam.domains.farm_world.FarmWorlds;
import bam.domains.grid_world.GridWorld;
import bam.domains.grid_world.GridWorlds;
import bam.human.analysis.*;
import org.json.JSONException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        // Path root = Paths.get("C:\\Users\\Tyler\\Desktop\\BAM - NIPS 2018\\human\\pilot_4_12");
        // DataSet data = DataSet.load(root, EventDecoder.JSON());

        // Path root = Paths.get("C:\\Users\\Tyler\\Desktop\\BAM - NIPS 2018\\human\\web_study_5_9\\web");
        // DataSet data = DataSet.load(root, EventDecoder.compressedJSON());

        Path root = Paths.get("C:\\Users\\Tyler\\Desktop\\BAM - NIPS 2018\\human\\mturk_study_5_11\\mturk");
        DataSet data = DataSet.load(root, EventDecoder.compressedJSON());

        // Print number of users
        System.out.println("total participants: " + data.participants().size());

        // Filter out incomplete and tutorial sessions
        SessionRecords all_complete = data.participants().filter(SessionFilter.tutorial(), SessionFilter.complete());

        System.out.println("Complete experimental sessions: " + all_complete.size());

        // Build list of environments to test
        Map<String, Environment> environments = new Hashtable<>();

        // environments.put("two-rooms", GridWorlds.twoRooms());
        // environments.put("doors", GridWorlds.doors());

        // environments.put("two-rooms", GridWorlds.twoRooms());
        // environments.put("doors", GridWorlds.doors());
        // environments.put("two-fields", FarmWorlds.twoFields());
        // environments.put("three-fields", FarmWorlds.threeFields());

        // Process data for each environment
        Path results = root.resolve("analysis");

        for(Map.Entry<String, Environment> entry : environments.entrySet()) {
            SessionRecords sessions = all_complete.filter(SessionFilter.environment(entry.getKey()));

            if(sessions.size() > 0)
                processEnvironment(sessions, entry.getValue(), results.resolve(entry.getKey()));
        }
    }

    private static void processEnvironment(SessionRecords sessions, Environment environment, Path directory) throws IOException {

        // Separate data by algorithm
        SessionRecords bam_sessions = sessions.filter(SessionFilter.algorithm("BAM"));
        SessionRecords model_based_sessions = sessions.filter(SessionFilter.algorithm("Model-Based"));
        SessionRecords cloning_sessions = sessions.filter(SessionFilter.algorithm("Cloning"));


        Performance.Evaluation evaluation = Performance.evaluation(500, environment);
        SessionAnnotation<Performance> performance_annotation = SessionAnnotation.performance(evaluation);

        List<AnnotatedSession<Performance>> bam_annotations = performance_annotation.of(bam_sessions);
        List<AnnotatedSession<Performance>> model_based_annotations = performance_annotation.of(model_based_sessions);
        List<AnnotatedSession<Performance>> cloning_annotations = performance_annotation.of(cloning_sessions);

        // Process data by number of actions
        TimeScale actions = TimeScale.actions();

        Condition bam_actions = Condition.of("BAM", bam_annotations, actions);
        Condition model_based_actions = Condition.of("Model-Based", model_based_annotations, actions);
        Condition cloning_actions = Condition.of("Cloning", cloning_annotations, actions);

        Condition.record(directory.resolve("actions"), actions, bam_actions, model_based_actions, cloning_actions);

        // Process data by number of episodes
        TimeScale episodes = TimeScale.episodes();

        Condition bam_episodes = Condition.of("BAM", bam_annotations, episodes);
        Condition model_based_episodes = Condition.of("Model-Based", model_based_annotations, episodes);
        Condition cloning_episodes = Condition.of("Cloning", cloning_annotations, episodes);

        Condition.record(directory.resolve("episodes"), episodes, bam_episodes, model_based_episodes, cloning_episodes);

        // Process data by demonstrations
        TimeScale demonstrations = TimeScale.demonstrations();

        Condition bam_demonstrations = Condition.of("BAM", bam_annotations, demonstrations);
        Condition model_based_demonstrations = Condition.of("Model-Based", model_based_annotations, demonstrations);
        Condition cloning_demonstrations = Condition.of("Cloning", cloning_annotations, demonstrations);

        Condition.record(directory.resolve("demonstrations"), demonstrations, bam_demonstrations, model_based_demonstrations, cloning_demonstrations);

        // Process data by wall-clock time
        TimeScale clock = TimeScale.clock(60000);

        Condition bam_clock = Condition.of("BAM", bam_annotations, clock);
        Condition model_based_clock = Condition.of("Model-Based", model_based_annotations, clock);
        Condition cloning_clock = Condition.of("Cloning", cloning_annotations, clock);

        Condition.record(directory.resolve("clock"), clock, bam_clock, model_based_clock, cloning_clock);

        // Print environment name and summary statistics
        System.out.println("\nProcessing data for: " + environment.name());
        System.out.println("BAM - " + bam_sessions.size() + " sessions");
        System.out.println("Model Based - " + model_based_sessions.size() + " sessions");
        System.out.println("Cloning - " + cloning_sessions.size() + " sessions");
    }
}
