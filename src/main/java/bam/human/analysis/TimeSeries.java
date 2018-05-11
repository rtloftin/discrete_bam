package bam.human.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a collection of time-series
 * for a set of sessions, with a common time
 * scale such as actions, demonstrations or
 * wall-clock time.
 */
public class TimeSeries<T> implements Iterable<List<T>> {

    public class Entry {
        private final List<T> data;
        private final int time;

        private Entry(int time) {
            this.data = new ArrayList<>();
            this.time = time;
        }

        private void add(T point) {
            data.add(point);
        }

        public int time() {
            return time;
        }

        public List<T> data() {
            return data;
        }
    }

    private final List<List<T>> sessions;
    private final int steps;

    private TimeSeries(List<List<T>> sessions) {
        this.sessions = sessions;
        int max_steps = 0;

        for (List<T> session : sessions)
            if(session.size() > max_steps)
                max_steps = session.size();

        this.steps = max_steps;
    }

    public static <T> TimeSeries of(List<AnnotatedSession<T>> sessions, TimeScale time_scale) {
        List<List<T>> series = new ArrayList<>();

        for (AnnotatedSession<T> session : sessions)
            series.add(time_scale.of(session));

        return new TimeSeries(series);
    }

    public int steps() { return steps; }

    public int size() {
        return sessions.size();
    }

    public List<T> get(int index) {
        return sessions.get(index);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return sessions.iterator();
    }
}
