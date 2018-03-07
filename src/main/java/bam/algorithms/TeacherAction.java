package bam.algorithms;

/**
 * A simple class representing a state-action pair.
 *
 * Created by Tyler on 7/22/2017.
 */
public class TeacherAction {

    public final int state;
    public final int action;

    public static TeacherAction of(int state, int action) {
        return new TeacherAction(state, action);
    }

    private TeacherAction(int state, int action) {
        this.state = state;
        this.action = action;
    }
}
