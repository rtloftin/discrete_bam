package bam.domains.gravity_world;

import java.awt.*;

public enum Colors {
    CLEAR (new Color(0f, 0f, 0f, 0f)),
    BLUE (Color.BLUE),
    ORANGE (Color.ORANGE),
    GREEN (Color.GREEN),
    PURPLE (Color.MAGENTA);

    public final Color paint;

    Colors(Color paint) { this.paint = paint; }
}
