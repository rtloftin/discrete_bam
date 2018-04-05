package bam.human.analysis;

import bam.algorithms.Behavior;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an event sequence taken from
 * a session, with each event being associated
 * with the agent's current behavior.
 */
public class Sequences<T> {

    private final List<Sequence<T>> sequences;

    private Sequences(List<Sequence<T>> sequences) {
        this.sequences = sequences;
    }

    public int size() {
        return sequences.size();
    }

    public Sequence get(int index) {
        return sequences.get(index);
    }

    public static <T> Sequences<T> of(Sessions sessions, Evaluation<T> evaluation) {
        List<Sequence<T>> sequences = new ArrayList<>();

        for(SessionRecord session : sessions.records()) {
            Sequence<T> sequence = Sequence.start();
            T performance = evaluation.of(Behavior.get());

            for(JSONObject event : session.events) {
                if(event.getString("type").equals("integrate"))
                    performance = evaluation.of(Behavior.load(event.getJSONObject("behavior")));

                sequence.add(event, performance);
            }

            sequences.add(sequence);
        }

        return new Sequences<>(sequences);
    }
}
