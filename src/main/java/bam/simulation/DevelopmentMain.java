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

        // cloningTest(root.get());
        // bamTest(root.get());
        // goalTest(root.get());
        commonTest(root.get());

    }

    private static void cloningTest(File root) throws Exception {

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
        ActionModel action_model = OldNormalizedActionModel.beta(1.0);

        // Task source
        Variational task_source = PointDensity.builder()
                .optimization(Momentum.with(0.01, 0.5))
                // .optimization(Adam.with(0.01, 0.8, 0.8, 0.05))
                .build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithms
        Algorithm bam = BAM.builder()
                .taskSource(task_source)
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
                // .dynamicsOptimization(Adam.with(0.01, 0.8, 0.8, 0.05))
                .planningAlgorithm(BoltzmannPlanner.algorithm( 1.0))
                .actionModel(action_model)
                .taskUpdates(20)
                .dynamicsUpdates(20)
                .emUpdates(40)
                .useTransitions(true)
                .build();

        // Initialize model-based algorithms
        Algorithm model = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
                // .dynamicsOptimization(Adam.with(0.01, 0.8, 0.8, 0.05))
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
        MultiTaskDemoExperiment experiment = MultiTaskDemoExperiment.builder()
                // .environments(empty, center_block, center_wall, two_rooms)
                // .environments(center_block, two_rooms, three_rooms)
                // .environments(flip)
                .environments(medium_flip)
                // .environments(large_flip)
                //.algorithms(bam, model, cloning)
                .algorithms(bam)
                .numSessions(10)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    private static void goalTest(File root) throws Exception {
        File folder = Util.stampedFolder("goal_test", root);

        Environment two_rooms = GridWorlds.twoRooms();
        Environment doors = GridWorlds.doors();

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

        // Initialize experiment
        MultiTaskGoalExperiment experiment = MultiTaskGoalExperiment.builder()
                // .environments(two_rooms, doors)
                .environments(two_fields, three_fields)
                .algorithms(bam, model)
                // .algorithms(bam)
                // .algorithms(model)
                .numSessions(30)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                //.finalNoop(false)
                .finalNoop(true)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    private static void commonTest(File root) throws Exception {
        File folder = Util.stampedFolder("common_test", root);

        Environment two_rooms = GridWorlds.twoRooms();
        Environment doors = GridWorlds.doors();

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
        Algorithm common = CommonReward.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(1.0, 0.7, 0.1))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        // Initialize experiment
        MultiTaskGoalExperiment experiment = MultiTaskGoalExperiment.builder()
                .environments(two_rooms, doors)
                // .environments(two_fields, three_fields)
                .algorithms(bam, common)
                // .algorithms(bam)
                // .algorithms(model)
                .numSessions(10)
                .maxDemonstrations(10)
                .evaluationEpisodes(50)
                //.finalNoop(false)
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
                .optimization(ClippedMomentum.with(0.01, 0.5, 0.2))
                .build();

        /* Variational task_source = GaussianDensity.builder()
                .optimization(AdaGrad.with(0.001, 0.7)).priorDeviation(1.0).numSamples(5).build(); */

        // Initialize BAM algorithms
        Algorithm bam = BAM.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(0.1, 0.5, 0.2))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
                // .dynamicsOptimization(Adam.with(0.01, 0.8, 0.8, 0.05))
                .planningAlgorithm(BoltzmannPlanner.algorithm( 1.0))
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .taskUpdates(20)
                .dynamicsUpdates(20)
                .emUpdates(10)
                .useTransitions(true)
                .build();

        // Initialize model-based algorithms
        Algorithm model = ModelBased.builder()
                .taskSource(task_source)
                .dynamicsOptimization(ClippedMomentum.with(0.1, 0.5, 0.2))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
                // .dynamicsOptimization(Adam.with(0.01, 0.8, 0.8, 0.05))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .taskUpdates(100)
                .dynamicsUpdates(100)
                .build();

        // Initialize cloning algorithms
        Algorithm cloning = Cloning.builder()
                .taskSource(task_source)
                .actionModel(action_model)
                .feedbackModel(feedback_model)
                .numUpdates(100)
                .build();

        // Initialize experiment
        MultiTaskFeedbackExperiment experiment = MultiTaskFeedbackExperiment.builder()
                .environments(two_rooms, doors)
                .environments(two_fields, three_fields)
                .algorithms(bam, model, cloning)
                //.algorithms(bam)
                .feedbackModel(feedback_model)
                .numSessions(10)
                .trainingEpisodes(50)
                .evaluationEpisodes(50)
                .build();

        // Run experiment
        experiment.run(folder);
    }

    /**
     * Opens a directory selection dialog, and returns the selected directory.
     *
     * @param start the directory which the dialog should initially display
     * @param message the message to be displayed at the top of the chooser window
     * @return optionally the selected directory, but empty if the user cancels
     */
    public static Optional<File> chooseFolder(File start, String message) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(start);
        chooser.setDialogTitle(message);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            return Optional.of(chooser.getSelectedFile());

        return Optional.empty();
    }

    /**
     * Attempts to set the specified preference for the given class and the current user.
     *
     * @param name the name of the preference
     * @param value the value of the preference
     */
    public static void setPreference(String name, String value) {
        try {
            Preferences.userRoot().put(name, value);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Attempts to get the specified preference for the given class and the current user.
     *
     * @param name the name of the preference
     * @return optionally the value of the preference, if it exists
     */
    public static Optional<String> getPreference(String name) {
        try{
            return Optional.ofNullable(Preferences.userRoot().get(name, null));
        } catch(Exception e) {
            System.out.println(e.getMessage());

            return Optional.empty();
        }
    }

    /**
     * Attempts to get the specified preference for the given class and the
     * current user. Returns the given default value if this isn't found.
     *
     * @param name the name of the preference
     * @param opt the default value to return if the preference isn't set
     * @return the value of the preference, or the default value if this doesn't exist
     */
    public static String getPreference(String name, String opt) {
        try{
            return Preferences.userRoot().get(name, opt);
        } catch(Exception e) {
            System.out.println(e.getMessage());

            return opt;
        }
    }

    /**
     * Displays the provided image in a new window.
     *
     * @param image the image to display
     * @param name the name of the image for the window title
     */
    public static void showImage(BufferedImage image, String name) {
        JFrame window = new JFrame(name);
        window.setSize(image.getWidth() + 20, image.getHeight() + 20);
        // window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.getContentPane().add(new JLabel(new ImageIcon(image)));
        window.pack();
        window.setVisible(true);
    }
}
