package bam.human.analysis;

import bam.algorithms.Behavior;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public interface SessionAnnotation<T> {

    static SessionAnnotation<Performance> performance(Performance.Evaluation evaluation) {
        return (SessionRecord session) -> {
            List<Performance> annotations = new ArrayList<>();
            Performance current_performance = evaluation.of(Behavior.get());

            for(JSONObject event : session.events) {
                if(event.getString("type").equals("integrate"))
                    current_performance = evaluation.of(Behavior.load(event.getJSONObject("behavior")));

                annotations.add(current_performance);
            }

            return AnnotatedSession.of(session.events, annotations);
        };
    }

    AnnotatedSession<T> of(SessionRecord session);

    default List<AnnotatedSession<T>> of(SessionRecords sessions) {
        List<AnnotatedSession<T>> annotations = new ArrayList<>();

        for(SessionRecord session : sessions)
            annotations.add(of(session));

        return annotations;
    }
}
