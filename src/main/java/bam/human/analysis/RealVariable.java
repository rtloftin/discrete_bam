package bam.human.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents a real-valued
 * random vector, for which we have
 * some number of independent samples.
 * Used to compute certain statistics
 * more easily.
 *
 * Created by Tyler on 4/21/2018.
 */
public class RealVariable {

    private List<double[]> samples;

    private int size;
    private boolean ready;

    private double[] mean;
    private double[] variance;
    private double[] deviation;
    private double[] error;

    private RealVariable() {
        ready = false;
        samples = new ArrayList<>();

        size = 0;
    }

    private void update() {
        if(!ready) {

            // Initialize variables
            mean = new double[size];
            variance = new double[size];
            deviation = new double[size];
            error = new double[size];

            for (int dim = 0; dim < size; ++dim) {
                int count = 0;

                // Compute mean
                mean[dim] = 0.0;

                for (double[] sample : samples) {
                    if(sample.length > dim) {
                        mean[dim] += sample[dim];
                        ++count;
                    }
                }

                mean[dim] /= count;

                // Compute variance
                variance[dim] = 0.0;

                for (double[] sample : samples) {
                    if(sample.length > dim) {
                        double del = sample[dim] - mean[dim];
                        variance[dim] += del * del;
                    }
                }

                variance[dim] = (1 == count) ? 0.0 : (variance[dim] / (count - 1) );

                // Compute deviation
                deviation[dim] = Math.sqrt(variance[dim]);

                // Compute error
                error[dim] = Math.sqrt(variance[dim] / count);
            }
        }

        ready = true;
    }

    public static RealVariable get() { return new RealVariable(); }

    public RealVariable add(double... sample) {
        ready = false;
        samples.add(sample);

        if(sample.length > size)
            size = sample.length;

        return this;
    }

    public int size() {
        return size;
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
