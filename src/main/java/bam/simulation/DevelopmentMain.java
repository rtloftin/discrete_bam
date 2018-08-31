package bam.simulation;

import bam.algorithms.*;
import bam.algorithms.action.ActionModel;
import bam.algorithms.action.NormalizedActionModel;
import bam.algorithms.alt.OldNormalizedActionModel;
import bam.algorithms.feedback.ASABL;
import bam.algorithms.feedback.FeedbackModel;
import bam.algorithms.optimization.Adam;
import bam.algorithms.optimization.ClippedMomentum;
import bam.algorithms.optimization.Momentum;
import bam.algorithms.planning.BoltzmannPlanner;
import bam.algorithms.variational.PointDensity;
import bam.algorithms.variational.Variational;
import bam.domains.Environment;
import bam.domains.NavGrid;
import bam.domains.farm_world.FarmWorlds;
import bam.domains.gravity_world.GravityWorlds;
import bam.domains.grid_world.GridWorlds;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * THIS IS THE FILE WE WRE ACTUALLY USING TO GENERATE SIMULATION DATA, NOT "SimulationMain.java" !!!
 *
 * This class is the entry point for interactive experiments
 * using BAM and related algorithms.  It is intended to
 * be used while testing the code and different parameter
 * values.  It should not be used for real data collection.
 */
public class DevelopmentMain {

    public static void main(String[] args) throws Exception {

        // Get the root directory
        String initial = Util.getPreference("root", System.getProperty("user.home"));
        Optional<File> root = Util.chooseFolder(new File(initial),
                "Please select a folder for results");

        if(!root.isPresent())
            throw new RuntimeException("User did not confirm a fucking directory to store the fucking data!");

        Util.setPreference("root", root.get().getPath());

        // fullTest(root.get());
        combinedTest(root.get());
        // goalTest(root.get());
        // commonTest(root.get());
        // feedbackTest(root.get());
    }

    private static void cloningTest(File root) throws Exception {

        // JUST EVALUATES BEHAVIORAL CLONING - NOTHING ELSE

        // Initialize data directory
        File folder = Util.stampedFolder("cloning_test", root);

        // Initialize test environments
        Environment empty = GridWorlds.empty(10, 10, NavGrid.FOUR);
        Environment center_block = GridWorlds.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorlds.centerWall(NavGrid.FOUR);
        Environment two_rooms = GridWorlds.twoRooms(NavGrid.FOUR);
        Environment three_rooms = GridWorlds.threeRooms(NavGrid.FOUR);

        Environment flip = GravityWorlds.flip();

        // Action Model
        ActionModel action_model = OldNormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(Momentum.with(0.01, 0.5)).build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize cloning algorithms
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .numUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskDemoExperiment experiment = MultiTaskDemoExperiment.builder()
                //.environments(two_rooms, three_rooms)
                // .environments(empty, center_block, center_wall, two_rooms)
                .environments(flip)
                .algorithms(cloning)
                .numSessions(20)
                .maxDemonstrations(20)
                .evaluationEpisodes(100)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    private static void bamTest(File root) throws Exception {

        // COMPARES BAM AGAINST CLONING AND MODEL-BASED IRL, BUT IN THE FIXED LENGTH DEMO SETTING

        File folder = Util.stampedFolder("bam_test", root);

        // Initialize test environments
        Environment empty = GridWorlds.empty(10, 10, NavGrid.FOUR);
        Environment center_block = GridWorlds.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorlds.centerWall(NavGrid.FOUR);
        Environment two_rooms = GridWorlds.twoRooms(NavGrid.FOUR);
        Environment three_rooms = GridWorlds.threeRooms(NavGrid.FOUR);

        Environment flip = GravityWorlds.flip();
        Environment medium_flip = GravityWorlds.medium_flip();
        Environment large_flip = GravityWorlds.large_flip();

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(ClippedMomentum.with(0.01, 0.7, 0.1))
                .build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithms
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

        // Initialize model-based algorithms
        Algorithm model = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize cloning algorithms
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .numUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskDemoExperiment experiment = MultiTaskDemoExperiment.builder()
                // .environments(empty, center_block, center_wall)
                .environments(center_block, two_rooms)
                .algorithms(bam, model, cloning)
                // .algorithms(bam)
                .numSessions(10)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    private static void fullTest(File root) throws Exception {

        // COMPARES ALL THE ALGORITHMS IN THE FULL LENGTH DEMO SETTING

        File folder = Util.stampedFolder("full_test", root);

        Environment center_block = GridWorlds.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorlds.centerWall(NavGrid.FOUR);
        Environment three_rooms = GridWorlds.threeRooms(NavGrid.FOUR);
        Environment two_rooms = GridWorlds.twoRooms();
        Environment doors = GridWorlds.doors();

        Environment flip = GravityWorlds.flip();
        Environment medium_flip = GravityWorlds.medium_flip();
        Environment choices = GravityWorlds.choices();
        Environment wall = GravityWorlds.wall();

        Environment two_fields = FarmWorlds.twoFields();
        Environment three_fields = FarmWorlds.threeFields();

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(ClippedMomentum.with(0.01, 0.7, 0.1))
                .build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithm
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

        // Initialize model-based algorithm
        Algorithm model = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize cloning algorithm
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .numUpdates(200)
                .build();

        Algorithm common_reward = CommonReward.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskDemoExperiment experiment = MultiTaskDemoExperiment.builder()
                // .environments(center_block, center_wall, three_rooms, two_rooms, doors)
                // .environments(center_block, center_wall)
                // .environments(flip, medium_flip, choices, wall)
                // .environments(medium_flip, choices)
                .environments(two_fields, three_fields)
                .algorithms(bam, model, common_reward, cloning)
                .numSessions(50)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                .build();



        // Run experiment
        experiment.run(folder);
    }

    private static void goalTest(File root) throws Exception {

        // EVALUATES ALL THE ALGORITHMS IN THE VARIABLE LENGTH (GOAL TERMINATED) DEMO SETTING

        File folder = Util.stampedFolder("goal_test", root);

        Environment center_block = GridWorlds.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorlds.centerWall(NavGrid.FOUR);
        Environment three_rooms = GridWorlds.threeRooms(NavGrid.FOUR);
        Environment two_rooms = GridWorlds.twoRooms();
        Environment doors = GridWorlds.doors();

        Environment flip = GravityWorlds.flip();
        Environment medium_flip = GravityWorlds.medium_flip();
        Environment choices = GravityWorlds.choices();
        Environment more_choices = GravityWorlds.more_choices();
        Environment big_wall = GravityWorlds.big_wall();
        Environment wall = GravityWorlds.wall();

        Environment two_fields = FarmWorlds.twoFields();
        Environment three_fields = FarmWorlds.threeFields();

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(ClippedMomentum.with(0.01, 0.7, 0.1))
                .build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithm
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

        // Initialize model-based algorithm
        Algorithm model = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize cloning algorithm
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .numUpdates(200)
                .build();

        Algorithm common_reward = CommonReward.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

                // Initialize experiment
        MultiTaskGoalExperiment experiment = MultiTaskGoalExperiment.builder()
                // .environments(center_block, center_wall, three_rooms, two_rooms, doors)
                // .environments(center_block, center_wall)
                // .environments(flip, medium_flip, choices, wall)
                // .environments(medium_flip, choices)
                .environments(more_choices, big_wall)
                // .environments(two_fields, three_fields)
                .algorithms(bam, model, cloning, common_reward)
                .numSessions(50)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                .finalNoop(true)
                .build();



        // Run experiment
        experiment.run(folder);
    }

    private static void combinedTest(File root) throws Exception {

        // EVALUATES BAM, MODEL-BASED IRL, AND CLONING WITH A COMBINATION OF FEEDBACK AND DEMONSTRATIONS

        File folder = Util.stampedFolder("combined_test", root);

        Environment center_block = GridWorlds.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorlds.centerWall(NavGrid.FOUR);
        Environment three_rooms = GridWorlds.threeRooms(NavGrid.FOUR);
        Environment two_rooms = GridWorlds.twoRooms();
        Environment doors = GridWorlds.doors();

        Environment flip = GravityWorlds.flip();
        Environment medium_flip = GravityWorlds.medium_flip();
        Environment choices = GravityWorlds.choices();
        Environment wall = GravityWorlds.wall();
        Environment more_choices = GravityWorlds.more_choices();
        Environment big_wall = GravityWorlds.big_wall();

        Environment two_fields = FarmWorlds.twoFields();
        Environment three_fields = FarmWorlds.threeFields();

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Feedback Model
        FeedbackModel feedback_model = ASABL.builder().build();

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(ClippedMomentum.with(0.01, 0.7, 0.1))
                .build();

        // Initialize BAM algorithm
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

        // Initialize model-based algorithm
        Algorithm model = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize cloning algorithm
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .numUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskCombinedExperiment experiment = MultiTaskCombinedExperiment.builder()
                // .environments(center_wall, three_rooms, two_rooms, doors)
                // .environments(medium_flip, choices, more_choices, big_wall)
                .environments(two_fields, three_fields)
                .algorithms(bam, model, cloning)
                .feedbackModel(feedback_model)
                .numSessions(50)
                .numEpisodes(10)
                .evaluationEpisodes(50)
                .finalNoop(true)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    private static void commonTest(File root) throws Exception {

        // EVALUATES BOTH COMMON REWARD LEARNING AND COMMON INTENT LEARNING (WHAT WAS THE DISTINCTION?)

        File folder = Util.stampedFolder("common_test", root);

        Environment center_block = GridWorlds.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorlds.centerWall(NavGrid.FOUR);
        Environment three_rooms = GridWorlds.threeRooms(NavGrid.FOUR);
        Environment two_rooms = GridWorlds.twoRooms();
        Environment doors = GridWorlds.doors();

        Environment flip = GravityWorlds.flip();
        Environment medium_flip = GravityWorlds.medium_flip();
        Environment choices = GravityWorlds.choices();
        Environment wall = GravityWorlds.wall();

        Environment two_fields = FarmWorlds.twoFields();
        Environment three_fields = FarmWorlds.threeFields();

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(ClippedMomentum.with(0.01, 0.7, 0.1))
                .build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithms
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

        // Initialize model-based algorithms
        Algorithm model = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize model-based algorithms
        Algorithm common_reward = CommonReward.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize model-based algorithms
        Algorithm common_intent = CommonIntent.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskGoalExperiment experiment = MultiTaskGoalExperiment.builder()
                // .environments(center_block, center_wall, three_rooms, two_rooms, doors)
                // .environments(center_block, center_wall)
                // .environments(flip, medium_flip, choices, wall)
                // .environments(medium_flip, choices)
                .environments(two_fields, three_fields)
                .algorithms(bam, model, common_reward, common_intent)
                .numSessions(50)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                .finalNoop(true)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    private static void feedbackTest(File root) throws Exception {

        File folder = Util.stampedFolder("feedback_test", root);

        // Initialize test environments
        Environment two_rooms = GridWorlds.twoRooms();
        Environment doors = GridWorlds.doors();

        Environment two_fields = FarmWorlds.twoFields();
        Environment three_fields = FarmWorlds.threeFields();

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Feedback Model
        FeedbackModel feedback_model = ASABL.builder().build();

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(ClippedMomentum.with(0.01, 0.7, 0.1))
                .build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithm
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

        // Initialize model-based algorithm
        Algorithm model = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize cloning algorithm
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .numUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskFeedbackExperiment experiment = MultiTaskFeedbackExperiment.builder()
                // .environments(two_rooms, doors)
                .environments(two_fields, three_fields)
                .algorithms(bam, model, cloning)
                .feedbackModel(feedback_model)
                .numSessions(20)
                .trainingEpisodes(50)
                .evaluationEpisodes(50)
                .build();

        // Run experiment
        experiment.run(folder);
    }
}
