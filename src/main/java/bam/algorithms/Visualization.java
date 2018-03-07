package bam.algorithms;

import java.awt.image.BufferedImage;

/**
 * Represents a named image.  The
 * image will typically only be
 * rendered when requested.
 *
 * Created by Tyler on 10/9/2017.
 */
public class Visualization {

    public final BufferedImage image;
    public final String name;

    public static Visualization of(BufferedImage image, String name) { return new Visualization(image, name); }

    public Visualization(BufferedImage image, String name) {
        this.image = image;
        this.name = name;
    }
}
