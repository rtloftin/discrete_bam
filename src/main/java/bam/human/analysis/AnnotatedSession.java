package bam.human.analysis;

import org.json.JSONObject;

import java.util.List;

/**
 * An individual session in which
 * each event is attached to an
 * annotation object.
 */
public class AnnotatedSession<T> {

    public final SessionRecord session;

    private List<JSONObject> events;
    private List<T> annotations;

    private AnnotatedSession(SessionRecord session, List<JSONObject> events, List<T> annotations) {
        this.session = session;
        this.events = events;
        this.annotations = annotations;
    }

    public static <T> AnnotatedSession<T> of(SessionRecord session, List<JSONObject> events, List<T> annotations) {
        return new AnnotatedSession<>(session, events, annotations);
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
