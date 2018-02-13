package bam;


import bam.algorithms.action.ActionModel;
import bam.algorithms.planning.BoltzmannPlanner;
import bam.algorithms.planning.MaxPlanner;
import bam.algorithms.variational.Variational;
import bam.domains.NavGrid;
import bam.util.Util;
import bam.algorithms.*;
import bam.algorithms.optimization.Momentum;
import bam.domains.grid_world.GridWorld;
import bam.algorithms.action.NormalizedActionModel;
import bam.algorithms.variational.PointDensity;

import java.io.File;
import java.util.Optional;

/**
 * This class holds the main entry points for
 * each of the simulation experiments we will do
 * with discrete BAM.
 *
 * These experiments will give us the data and
 * visualizations necessary for my oral document,
 * and will allow us to perfect discrete BAM
 * in isolation, before moving to the user-studies.
 *
 * Created by Tyler on 7/22/2017.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // Get the root directory
        String initial = Util.getPreference("root", System.getProperty("user.home"));
        Optional<File> root = Util.chooseFolder(new File(initial),
                "Please select a folder for results");

        if(!root.isPresent())
            throw new RuntimeException("User did not confirm a fucking directory to store the fucking data!");

        Util.setPreference("root", root.get().getPath());

        // singleTaskDemoExperiment(root.get());
        // multiTaskDemoExperiment(root.get());

        // cloningTest(root.get());
        bamTest(root.get());
    }

    /////////////////////////////////////////////////////////////////////////
    // Experimental Methods -- use these to actually generate useful data //
    ////////////////////////////////////////////////////////////////////////

    private static void singleTaskDemoExperiment(File root) throws Exception {

        // Initialize data directory
        File folder = Util.stampedFolder("single_task", root);

        // Initialize test environments
        Environment empty = GridWorld.empty(10, 10, NavGrid.FOUR);
        Environment center_block = GridWorld.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorld.centerWall(NavGrid.FOUR);
        Environment two_rooms = GridWorld.twoRooms(NavGrid.FOUR);

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(Momentum.with(0.01, 0.5)).build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithms
        Algorithm bam = BAM.builder()
                .taskSource(task_source)
                .dynamicsOptimization(Momentum.with(0.1, 0.5))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
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
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize cloning algorithms
        Algorithm cloning = Cloning.builder()
                .taskSource(PointDensity.builder()
                        .optimization(Momentum.with(0.001, 0.5)).build())
                .actionModel(action_model)
                .numUpdates(200)
                .build();

        Algorithm irl = MLIRL.builder()
                .planningAlgorithm(MaxPlanner.algorithm())
                .taskSource(PointDensity.builder().build())
                .actionModel(action_model)
                .taskUpdates(200)
                .build();

        // Initialize experiment
        SingleTaskExperiment experiment = SingleTaskExperiment.builder()
                .environments(empty, center_block, center_wall, two_rooms)
                .algorithms(bam, model, cloning, irl)
                .numSessions(50)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    private static void multiTaskDemoExperiment(File root) throws Exception {

        // Initialize data directory
        File folder = Util.stampedFolder("multi_task", root);

        // Initialize test environments
        Environment empty = GridWorld.empty(10, 10, NavGrid.FOUR);
        Environment center_block = GridWorld.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorld.centerWall(NavGrid.FOUR);
        Environment two_rooms = GridWorld.twoRooms(NavGrid.FOUR);
        Environment three_rooms = GridWorld.threeRooms(NavGrid.FOUR);

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(Momentum.with(0.01, 0.5)).build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithms
        Algorithm bam = BAM.builder()
                .taskSource(task_source)
                .dynamicsOptimization(Momentum.with(0.1, 0.5))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
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
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize cloning algorithms
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .numUpdates(200)
                .build();

        Algorithm irl = MLIRL.builder()
                .planningAlgorithm(MaxPlanner.algorithm())
                .taskSource(task_source)
                .actionModel(action_model)
                .taskUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskExperiment experiment = MultiTaskExperiment.builder()
                .environments(empty, center_block, center_wall, two_rooms, three_rooms)
                .algorithms(bam, model, cloning, irl)
                .numSessions(50)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    /////////////////////////////////////////////
    // Test Methods -- Not for data collection //
    /////////////////////////////////////////////

    private static void cloningTest(File root) throws Exception {

        // Initialize data directory
        File folder = Util.stampedFolder("cloning_test", root);

        // Initialize test environments
        Environment empty = GridWorld.empty(10, 10, NavGrid.FOUR);
        Environment center_block = GridWorld.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorld.centerWall(NavGrid.FOUR);
        Environment two_rooms = GridWorld.twoRooms(NavGrid.FOUR);
        Environment three_rooms = GridWorld.threeRooms(NavGrid.FOUR);

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

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
        MultiTaskExperiment experiment = MultiTaskExperiment.builder()
                //.environments(two_rooms, three_rooms)
                .environments(empty, center_block, center_wall, two_rooms)
                .algorithms(cloning)
                .numSessions(20)
                .maxDemonstrations(20)
                .evaluationEpisodes(100)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    private static void bamTest(File root) throws Exception {

        File folder = Util.stampedFolder("bam_test", root);

        // Initialize test environments
        Environment empty = GridWorld.empty(10, 10, NavGrid.FOUR);
        Environment center_block = GridWorld.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorld.centerWall(NavGrid.FOUR);
        Environment two_rooms = GridWorld.twoRooms(NavGrid.FOUR);
        Environment three_rooms = GridWorld.threeRooms(NavGrid.FOUR);

        // Action Model
        ActionModel action_model = NormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(Momentum.with(0.01, 0.5)).build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithms
        Algorithm bam = BAM.builder()
                .taskSource(task_source)
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
                .planningAlgorithm(BoltzmannPlanner.algorithm( 1.0))
                .actionModel(action_model)
                .taskUpdates(10)
                .dynamicsUpdates(10)
                .emUpdates(40)
                .useTransitions(true)
                .build();

        // Initialize model-based algorithms
        Algorithm model = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(400)
                .dynamicsUpdates(400)
                .build();

        // Initialize cloning algorithms
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .numUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskExperiment experiment = MultiTaskExperiment.builder()
                .environments(center_block, center_wall, two_rooms)
                .algorithms(bam, model, cloning)
                .numSessions(10)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                .build();

        // Run experiment
        experiment.run(folder);
    }
}
