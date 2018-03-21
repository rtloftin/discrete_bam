package bam.simulation;


import bam.algorithms.action.ActionModel;
import bam.algorithms.planning.BoltzmannPlanner;
import bam.algorithms.planning.MaxPlanner;
import bam.algorithms.variational.Variational;
import bam.domains.Environment;
import bam.domains.NavGrid;
import bam.domains.gravity_world.GravityWorld;
import bam.algorithms.*;
import bam.algorithms.optimization.Momentum;
import bam.domains.grid_world.GridWorlds;
import bam.algorithms.action.NormalizedActionModel;
import bam.algorithms.variational.PointDensity;

import java.io.File;
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

        // Try to load config file

        // If failed, generate skeleton config file and exit

        // Otherwise, configure and run main experiment

        // Get the root directory
        String initial = Util.getPreference("root", System.getProperty("user.home"));
        Optional<File> root = Util.chooseFolder(new File(initial),
                "Please select a folder for results");

        if(!root.isPresent())
            throw new RuntimeException("User did not confirm a fucking directory to store the fucking data!");

        Util.setPreference("root", root.get().getPath());

        // singleTaskDemoExperiment(root.get());
        multiTaskDemoExperiment(root.get());

        // cloningTest(root.get());
        // bamTest(root.get());
    }

    /////////////////////////////////////////////////////////////////////////
    // Experimental Methods -- use these to actually generate useful data //
    ////////////////////////////////////////////////////////////////////////

    private static void singleTaskDemoExperiment(File root) throws Exception {

        // Initialize data directory
        File folder = Util.stampedFolder("single_task", root);

        // Initialize test environments
        Environment empty = GridWorlds.empty(10, 10, NavGrid.FOUR);
        Environment center_block = GridWorlds.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorlds.centerWall(NavGrid.FOUR);
        Environment two_rooms = GridWorlds.twoRooms(NavGrid.FOUR);

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
        SingleTaskDemoExperiment experiment = SingleTaskDemoExperiment.builder()
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
        Environment empty = GridWorlds.empty(10, 10, NavGrid.FOUR);
        Environment center_block = GridWorlds.centerBlock(NavGrid.FOUR);
        Environment center_wall = GridWorlds.centerWall(NavGrid.FOUR);
        Environment two_rooms = GridWorlds.twoRooms(NavGrid.FOUR);
        Environment three_rooms = GridWorlds.threeRooms(NavGrid.FOUR);

        Environment flip = GravityWorld.flip();
        Environment medium_flip = GravityWorld.medium_flip();
        Environment large_flip = GravityWorld.large_flip();

        Environment wall = GravityWorld.wall();
        Environment choices = GravityWorld.choices();

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
        MultiTaskDemoExperiment experiment = MultiTaskDemoExperiment.builder()
                // .environments(empty, center_block, center_wall, two_rooms, three_rooms)
                // .environments(flip, medium_flip)
                .environments(wall, choices)
                // .algorithms(bam, model, cloning, irl)
                .algorithms(bam, model, cloning)
                .numSessions(50)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                .build();

        // Run experiment
        experiment.run(folder);
    }
}
