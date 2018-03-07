package bam.algorithms;

/**
 * A simple class representing a single feedback
 * signal for a particular state and action.
 *
 * Created by Tyler on 7/22/2017.
 */
public class TeacherFeedback {

    public final int state;
    public final int action;
    public final double value;

    public static TeacherFeedback of(int state, int action, double value) {
        return new TeacherFeedback(state, action, value);
    }

    public TeacherFeedback(int  state, int action, double value) {
        this.state = state;
        this.action = action;
        this.value = value;
    }
}
