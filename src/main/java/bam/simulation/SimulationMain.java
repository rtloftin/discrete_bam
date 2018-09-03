package bam.simulation;


import bam.algorithms.action.ActionModel;
import bam.algorithms.action.NormalizedActionModel;
import bam.algorithms.feedback.ASABL;
import bam.algorithms.feedback.FeedbackModel;
import bam.algorithms.optimization.ClippedMomentum;
import bam.algorithms.planning.BoltzmannPlanner;
import bam.algorithms.variational.Variational;
import bam.domains.Environment;
import bam.algorithms.*;
import bam.algorithms.variational.PointDensity;
import bam.domains.NavGrid;
import bam.domains.farm_world.FarmWorlds;
import bam.domains.gravity_world.GravityWorlds;
import bam.domains.grid_world.GridWorlds;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

/**
 * This class is the entry point for batch
 * simulation experiments comparing discrete
 * BAM against other algorithms.  It is intended
 * to be run from a jar file on a remote machine,
 * and should get all required configuration
 * information from a configuration file, such
 * that we do not need to recompile for each new
 * set of experiments.
 */
public class SimulationMain {

    public static void main(String[] args) throws Exception {

        // Define learning environments
        HashMap<String, Environment> environments = new HashMap<>();
        environments.put("center_wall", GridWorlds.centerWallLarge(NavGrid.FOUR));
        environments.put("three_rooms", GridWorlds.threeRoomsLarge(NavGrid.FOUR));
        environments.put("two_rooms", GridWorlds.twoRooms());
        environments.put("doors", GridWorlds.doors());
        environments.put("two_fields", FarmWorlds.twoFields());
        environments.put("three_fields", FarmWorlds.threeFields());
        environments.put("two_colors", GravityWorlds.twoColors());
        environments.put("three_colors", GravityWorlds.threeColors());

        // Get configuration
        File data_root = new File(System.getProperty("user.home"));
        boolean include_feedback = false;
        Environment environment = environments.get("three_colors");

        // Check if we were given the correct command line arguments
        if(2 == args.length) {

            if(environments.containsKey(args[0]))
                environment = environments.get(args[0]);

            include_feedback = args[1].equals("combined");
        } else {

            // Select folder
            Optional<File> selection = Util
                    .chooseFolder(new File(Util.getPreference("data_root", System.getProperty("user.home"))),
                            "Please select a folder for results");

            if(!selection.isPresent())
                throw new RuntimeException("User did not confirm a fucking directory to store the fucking data!");

            data_root = selection.get();
            Util.setPreference("data_root", data_root.getPath());

            // Select experiment type
            include_feedback = !Util.yesOrNo("Use only demonstrations, no feedback?");
        }

        // Run experiments
        if(include_feedback) {
            demonstrationFeedbackExperiment(data_root, environment);
        } else {
            demonstrationExperiment(data_root, environment);
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // Experimental Methods -- use these to actually generate useful data //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Runs experiments with only goal-terminated demonstrations.  Compares BAM against
     * model-based IRL, model-based IRL with a global cost function, and behavioral cloning.
     *
     * @param data_root the root directory where the data should be stored, a new subdirectory will be created
     * @param environments the list of environments in which the algorithms should be evaluated.
     * @throws Exception if any error occurs (duh)
     */
    private static void demonstrationExperiment(File data_root, Environment... environments) throws Exception {

        // Data directory
        File folder = Util.stampedFolder("demonstration_experiment", data_root);

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(ClippedMomentum.with(0.01, 0.7, 0.1))
                .build();

        // BAM algorithm
        Algorithm bam = BAM.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm( 1.0))
                .actionModel(action_model)
                .taskUpdates(20)
                .dynamicsUpdates(20)
                .emUpdates(10)
                .useTransitions(true)
                .build();

        // Model-based IRL
        Algorithm model_based = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Behavioral cloning
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .numUpdates(200)
                .build();

        // Global cost
        Algorithm global_cost = CommonReward.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskGoalExperiment experiment = MultiTaskGoalExperiment.builder()
                .environments(environments)
                .algorithms(bam, model_based, cloning, global_cost)
                .numSessions(300)
                .maxDemonstrations(10)
                .evaluationEpisodes(100)
                .finalNoop(true)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    /**
     * Runs  experiments with both goal-terminated demonstrations and evaluative feedback.  Compares BAM against
     * model-based IRL, model-based IRL with a global cost function, and behavioral cloning.
     *
     * @param data_root the root directory where the data should be stored, a new subdirectory will be created
     * @param environments the list of environments in which the algorithms should be evaluated.
     * @throws Exception if any error occurs (duh)
     */
    private static void demonstrationFeedbackExperiment(File data_root, Environment... environments) throws Exception {

        // Data directory
        File folder = Util.stampedFolder("demonstration_feedback_experiment", data_root);

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Feedback Model
        FeedbackModel feedback_model = ASABL.builder().build();

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(ClippedMomentum.with(0.01, 0.7, 0.1))
                .build();

        // BAM algorithm
        Algorithm bam = BAM.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm( 1.0))
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .taskUpdates(20)
                .dynamicsUpdates(20)
                .emUpdates(10)
                .useTransitions(true)
                .build();

        // Model-based IRL
        Algorithm model_based = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Behavioral cloning
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .numUpdates(200)
                .build();

        // Global cost
        Algorithm global_cost = CommonReward.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskCombinedExperiment experiment = MultiTaskCombinedExperiment.builder()
                .environments(environments)
                .algorithms(bam, model_based, cloning, global_cost)
                .feedbackModel(feedback_model)
                .numSessions(300)
                .numEpisodes(10)
                .evaluationEpisodes(100)
                .finalNoop(true)
                .build();

        // Run experiment
        experiment.run(folder);
    }
}
