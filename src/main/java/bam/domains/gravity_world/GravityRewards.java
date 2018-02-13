package bam.domains.gravity_world;

import bam.RewardMapping;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class GravityRewards implements RewardMapping {

    private final int width;
    private final int height;
    private final int num_cells;

    GravityRewards(int width, int height) {
        this.width = width;
        this.height = height;

        num_cells = width * height;
    }

    @Override
    public double reward(int state, double[] intent) {
        return intent[state % num_cells];
    }

    @Override
    public void gradient(int state, double[] intent, double weight, double[] gradient) {
        gradient[state % num_cells] += weight;
    }

    @Override
    public int intentSize() {
        return num_cells;
    }

    @Override
    public Optional<BufferedImage> render(double[] intent) {

        // Get reward ranges
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for(int state = 0; state < intent.length; ++state) {
            if(intent[state] < min)
                min = intent[state];
            if(intent[state] > max)
                max = intent[state];
        }

        double range = 0.01 + (float)(max - min);

        // Render image
        BufferedImage image = new BufferedImage(width * GravityWorld.SCALE,
                height * GravityWorld.SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.translate(0, image.getHeight());
        graphics.scale(1, -1);

        for(int row = 0; row < height; ++row)
            for(int column = 0; column < width; ++column) {
                float hue = 0.65f * (float)(1.0 - (intent[row + (column * height)] - min) / range);

                graphics.setPaint(Color.getHSBColor(hue, 1f, 1f));
                graphics.fillRect(column * GravityWorld.SCALE,
                        row * GravityWorld.SCALE, GravityWorld.SCALE, GravityWorld.SCALE);
            }

        return Optional.of(image);
    }
}
