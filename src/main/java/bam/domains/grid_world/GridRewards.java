package bam.domains.grid_world;

import bam.algorithms.RewardMapping;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

class GridRewards implements RewardMapping {

    private int width, height;

    GridRewards(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double reward(int state, double[] intent) {
        return intent[state];
    }

    @Override
    public void gradient(int state, double[] intent, double weight, double[] gradient) {
        gradient[state] += weight;
    }

    @Override
    public int intentSize() {
        return width * height;
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
        BufferedImage image = new BufferedImage(width * GridWorld.SCALE,
                height * GridWorld.SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.translate(0, image.getHeight());
        graphics.scale(1, -1);

        for(int row = 0; row < height; ++row)
            for(int column = 0; column < width; ++column) {
                float hue = 0.65f * (float)(1.0 - (intent[row + (column * height)] - min) / range);

                graphics.setPaint(Color.getHSBColor(hue, 1f, 1f));
                graphics.fillRect(column * GridWorld.SCALE,
                        row * GridWorld.SCALE, GridWorld.SCALE, GridWorld.SCALE);
            }

        return Optional.of(image);
    }
}
