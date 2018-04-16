package bam.human.analysis;

import org.json.JSONObject;

import java.util.List;

/**
 * An individual session in which
 * each event is attached to an
 * annotation object.
 */
public class AnnotatedSession<T> {

    private List<JSONObject> events;
    private List<T> annotations;

    private AnnotatedSession(List<JSONObject> events, List<T> annotations) {
        this.events = events;
        this.annotations = annotations;
    }

    public static <T> AnnotatedSession<T> of(List<JSONObject> events, List<T> annotations) {
        return new AnnotatedSession<>(events, annotations);
    }

    public int size() {
        return events.size();
    }

    public JSONObject event(int index) {
        return events.get(index);
    }

    public T annotation(int index) {
        return annotations.get(index);
    }
}
