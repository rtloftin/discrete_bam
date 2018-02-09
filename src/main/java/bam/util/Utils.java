package bam.util;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * This class holds static utility
 * methods for various tasks that
 * we have frequently required.
 *
 * Created by Tyler on 9/26/2017.
 */
public class Utils {

    /**
     * Creates and returns a unique, timestamped directory
     * to hold experimental results
     *
     * @param parent the parent directory in which we will create the data directory
     * @return the directory path
     */
    public static File dataFolder(File parent) {

        // Initialize time
        Instant time = Instant.now();

        // Initialize number format
        DateTimeFormatter time_format = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm").withZone(ZoneId.systemDefault());

        // Cycle through ids until we find one that does not exist
        File folder;
        int id = 0;

        do {
            folder = new File(parent, time_format.format(time) + "_" +  String.format("%02d", id++) + File.separator);
        } while(folder.exists());

        // Create the directory
        folder.mkdirs();

        // Return the directory
        return folder;
    }

    /**
     * Displays the provided image in a new window.
     *
     * @param image the image to display
     * @param name the name of the image for the window title
     */
    public static void display(BufferedImage image, String name) {
        JFrame window = new JFrame(name);
        window.setSize(image.getWidth() + 20, image.getHeight() + 20);
        // window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.getContentPane().add(new JLabel(new ImageIcon(image)));
        window.pack();
        window.setVisible(true);
    }
}
