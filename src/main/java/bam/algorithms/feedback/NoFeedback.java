package bam.algorithms.feedback;

import java.util.Random;

/**
 * A dummy feedback model good for cases
 * where we don't actually plan on using
 * evaluative feedback.
 *
 * Created by Tyler on 10/30/2017.
 */
public class NoFeedback implements FeedbackModel {

    private static NoFeedback instance = new NoFeedback();

    public static NoFeedback get() { return instance; }

    private NoFeedback() {}

    @Override
    public double feedback(int action, double[] values, Random random) {
        return 0.0;
    }

    @Override
    public void gradient(double feedback, int action, double[] values, double[] gradient, double weight) {}

    @Override
    public String name() {
        return "No Feedback";
    }
}
