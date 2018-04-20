package bam.algorithms.feedback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * A version of the ASABL
 * model using the uniform
 * policy to compute the
 * advantage of an action.
 *
 * We may abstract the process of
 * computing the advantage later.
 *
 * Created by Tyler on 8/27/2017.
 */
public class ASABL implements FeedbackModel {

    public static class Builder {

        private AdvantageModel advantage_model = MeanAdvantage.get(); // Model of how the teacher computes the advantage

        private double epsilon = 0.05; // Teacher error rate
        private double alpha = 1.0; // The scale of the feedback distribution
        private double mu_plus = 0.1; // Probability of implicit positive feedback
        private double mu_minus = 0.1; // Probability of implicit negative feedback.

        public Builder advantage(AdvantageModel advantage_model) {
            this.advantage_model = advantage_model;

            return this;
        }

        public Builder epsilon(double epsilon) {
            this.epsilon = epsilon;

            return this;
        }

        public Builder alpha(double alpha) {
            this.alpha = alpha;

            return this;
        }

        public Builder muPlus(double mu_plus) {
            this.mu_plus = mu_plus;

            return this;
        }

        public Builder muMinus(double mu_minus) {
            this.mu_minus = mu_minus;

            return this;
        }

        public ASABL build() {
            return new ASABL(advantage_model, epsilon, alpha, mu_plus, mu_minus);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ASABL load(JSONObject config) throws JSONException {
        return builder()
                .advantage(AdvantageModel.load(config.getJSONObject("advantage")))
                .epsilon(config.getDouble("epsilon"))
                .alpha(config.getDouble("alpha"))
                .muPlus(config.getDouble("mu plus"))
                .muMinus(config.getDouble("mu minus"))
                .build();
    }

    private AdvantageModel advantage_model; // The model of how the teacher computes the advantage

    private final double epsilon; // TeacherFeedback error rate
    private final double alpha; // The scale of the feedback distribution
    private final double mu_plus; // Probability of implicit positive feedback
    private final double mu_minus; // Probability of implicit negative feedback.

    private ASABL(AdvantageModel advantage_model,
                  double epsilon,
                  double alpha,
                  double mu_plus,
                  double mu_minus) {
        this.advantage_model = advantage_model;
        this.epsilon = epsilon;
        this.alpha = alpha;
        this.mu_plus = mu_plus;
        this.mu_minus = mu_minus;
    }

    @Override
    public double feedback(int action, double[] values, Random random) {
        double sig = 1.0 / (1.0 + Math.exp(-alpha * advantage_model.advantage(action, values)));
        double rand = random.nextDouble();

        // Check for positive
        double total = (1.0 - mu_plus) * (epsilon + (1.0 - 2.0 * epsilon) * sig);

        if(rand <= total)
            return 1.0;

        // Check for negative
        total += (1.0 - mu_minus) * (epsilon + (1.0 - 2.0 * epsilon) * (1.0 - sig));

        if(rand <= total)
            return -1.0;

        // Return neutral
        return 0.0;
    }

    @Override
    public void gradient(double feedback, int action, double[] values, double[] gradient, double weight) {
        double sig = 1.0 / (1.0 + Math.exp(-alpha * advantage_model.advantage(action, values)));
        double err = 1.0 - (2.0 * epsilon);
        double derivative = alpha * sig * (1.0 - sig);

        if(0.0 < feedback) {
            derivative *= err / (epsilon + err * sig);
        } else if(0.0 > feedback) {
            derivative *= -err / (epsilon + err * (1.0 - sig));
        } else {
            derivative *= err * (mu_plus - mu_minus);
            derivative /= ( (mu_plus * (epsilon + (err * sig) ) ) + (mu_minus * (epsilon + (err * (1.0 - sig) ) ) ) );
        }

        // if(!Double.isFinite(derivative))
           // throw new RuntimeException("ASABL model encountered infinity");

        advantage_model.gradient(action, values, gradient, weight * derivative);
    }

    /**
     * Gets the name of this feedback model.
     *
     * @return the name of this feedback model
     */
    @Override
    public String name() {
        return "A-SABL";
    }

    /**
     * Gets a JSON representation of this model.
     *
     * @return a json representation of this model
     * @throws JSONException
     */
    @Override
    public JSONObject serialize() throws JSONException {
        return new JSONObject()
                .put("name", name())
                .put("class", getClass().getSimpleName())
                .put("advantage", advantage_model.serialize())
                .put("epsilon", epsilon)
                .put("alpha", alpha)
                .put("mu plus", mu_plus)
                .put("mu minus", mu_minus);
    }
}
