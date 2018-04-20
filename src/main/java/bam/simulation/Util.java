package bam.simulation;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * This class holds static utility
 * methods for various tasks that
 * we have frequently required.
 *
 * Created by Tyler on 9/26/2017.
 */
public class Util {

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
     * Creates and returns a unique, time stamped directory with the given name.
     *
     * @param name the name to prepended to the name of the directory.
     * @param parent the parent directory in which we will create the data directory
     * @return the directory path
     */
    public static File stampedFolder(String name, File parent) {

        // Initialize time
        Instant time = Instant.now();

        // Initialize number format
        DateTimeFormatter time_format = DateTimeFormatter
                .ofPattern("yyyy-MM-dd_HH-mm")
                .withZone(ZoneId.systemDefault());

        // Cycle through ids until we find one that does not exist
        File folder = new File(parent, name + "_" + time_format.format(time) + File.separator);
        int id = 1;

        while(folder.exists())
            folder = new File(parent, name + "_" + time_format.format(time)
                    + "_" +  String.format("%02d", id++) + File.separator);

        // Create the directory
        if(!folder.mkdirs())
            throw new RuntimeException("Could not create data directory");

        // Return the directory
        return folder;
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
