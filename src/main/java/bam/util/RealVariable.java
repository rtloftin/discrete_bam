package bam.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents a real-valued
 * random vector, for which we have
 * some number of independent samples.
 * Used to compute certain statistics
 * more easily.
 *
 * Created by Tyler on 10/10/2017.
 */
public class RealVariable {

    private List<double[]> samples;

    private int size;
    private boolean ready;

    private double[] mean;
    private double[] variance;
    private double[] deviation;
    private double[] error;

    private RealVariable(int size) {
        this.size = size;
        ready = false;

        mean = new double[size];
        variance = new double[size];
        deviation = new double[size];
        error = new double[size];

        samples = new ArrayList<>();
    }

    private void update() {
        if(!ready)
            for(int dim = 0; dim < size; ++dim) {

                // Compute mean
                mean[dim] = 0.0;

                for(double[] sample : samples)
                    mean[dim] += sample[dim];

                mean[dim] /= samples.size();

                // Compute variance
                variance[dim] = 0.0;

                for(double[] sample: samples) {
                    double del = sample[dim] - mean[dim];
                    variance[dim] += del * del;
                }

                variance[dim] /= (samples.size() - 1);

                // Compute deviation
                deviation[dim] = Math.sqrt(variance[dim]);

                // Compute error
                error[dim] = Math.sqrt(variance[dim] / samples.size());
            }

        ready = true;
    }

    public static RealVariable scalar() { return new RealVariable(1); }

    public static RealVariable vector(int size) { return new RealVariable(size); }

    public RealVariable add(double... sample) {
        ready = false;
        samples.add(sample);

        return this;
    }

    public double[] mean() {
        update();

        return mean;
    }

    public double mean(int dimension) {
        update();

        return mean()[dimension];
    }

    public double[] variance() {
        update();

        return variance;
    }

    public double variance(int dimension) {
        update();

        return variance()[dimension];
    }

    public double[] deviation() {
        update();

        return deviation;
    }

    public double deviation(int dimension) {
        update();

        return deviation()[dimension];
    }

    public double[] error() {
        update();

        return error;
    }

    public double error(int dimension) {
        update();

        return deviation()[dimension];
    }
}
