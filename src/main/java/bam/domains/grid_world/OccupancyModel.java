package bam.domains.grid_world;

import bam.DynamicsModel;
import bam.algorithms.optimization.Optimization;
import bam.domains.NavGrid;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;

/**
 * A learned model represented by
 * and occupancy map.
 *
 * Created by Tyler on 10/9/2017.
 */
class OccupancyModel implements DynamicsModel {

    // private final double alpha = 1.0;
    // private final double beta = 5.0;

    private final double prior = -3.0;

    private NavGrid grid;

    private double[] parameters;
    private double[] gradient;

    private Optimization.Instance optimizer = null;

    private double occupied(int index) {
        return 1.0 / (1.0 + Math.exp(-parameters[index]));
    }

    OccupancyModel(NavGrid grid) {
        this.grid = grid;

        parameters = new double[grid.numStates()];
        gradient = new double[grid.numStates()];
    }

    @Override
    public int numStates() { return grid.numStates(); }

    @Override
    public int numActions(int state) { return grid.numActions(); }

    @Override
    public int depth() { return grid.depth(); }

    @Override
    public int[] successors(int state, int action) { return new int[]{state, grid.next(state, action)}; }

    @Override
    public double[] transitions(int state, int action) {
        double collision = occupied(grid.next(state, action));

        return new double[]{collision, 1.0 - collision};
    }

    @Override
    public void initialize(Optimization optimization) {
        Arrays.fill(parameters, prior);
        Arrays.fill(gradient, 0.0);

        optimizer = optimization.instance(parameters.length);
    }

    @Override
    public void train(int start, int action, int end, double weight) {
        int next = grid.next(start, action);

        if (next != start) { // If this action leads to a self transition, it tells us nothing
            double collision = occupied(next);

            if (start == end) { // Collision
                gradient[next] += weight * (1.0 - collision);
            } else if (next == end) { // No collision
                gradient[next] -= weight * collision;
            }
        }
    }

    @Override
    public void update() {

        // Check if learner was initialized
        if (null == optimizer)
            throw new RuntimeException("Optimization algorithm not initialized");

        /* for (int state = 0; state < grid.numStates(); ++state) {
               double p = occupied(state);
               gradient[state] += alpha * (1.0 - p) - beta * p;
           } */

        // Perform update
        optimizer.update(parameters, gradient);

        // Reset gradient
        Arrays.fill(gradient, 0.0);
    }

    @Override
    public void clear() { Arrays.fill(gradient, 0.0); }

    @Override
    public Optional<BufferedImage> render() {
        BufferedImage image = new BufferedImage(grid.width() * GridWorld.SCALE,
                grid.height() * GridWorld.SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.translate(0, image.getHeight());
        graphics.scale(1, -1);

        for(int row = 0; row < grid.height(); ++row)
            for(int column = 0; column < grid.width(); ++column) {
                float hue = 0.65f * (float)(1.0 - occupied(grid.index(row, column)));

                graphics.setPaint(Color.getHSBColor(hue, 1f, 1f));
                graphics.fillRect(column * GridWorld.SCALE,
                        row * GridWorld.SCALE, GridWorld.SCALE, GridWorld.SCALE);
            }

        return Optional.of(image);
    }
}
