package bam;

/**
 * A simple class representing a state transition.
 *
 * Created by Tyler on 7/22/2017.
 */
public class StateTransition {

    public final int start;
    public final int action;
    public final int end;

    public static StateTransition of(int start, int action, int end) {
        return new StateTransition(start, action, end);
    }

    public StateTransition(int start, int action, int end) {
        this.start = start;
        this.action = action;
        this.end = end;
    }
}
