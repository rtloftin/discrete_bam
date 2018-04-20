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
public class TimeSeries<T> implements Iterable<TimeSeries<T>.Entry> {

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

    private final List<Entry> entries;

    private final TimeScale time_scale;
    private final int start;
    private final int end;

    private TimeSeries(List<List<T>> sessions, TimeScale time_scale) {
        this.entries = new ArrayList<>();

        for (List<T> session : sessions) {
            for (int i = 0; i < session.size(); ++i) {
                if (entries.size() <= i)
                    entries.add(this.new Entry(time_scale.time(i)));

                entries.get(i).add(session.get(i));
            }
        }

        this.time_scale = time_scale;
        this.start = time_scale.time(0);
        this.end = time_scale.time(Math.max(entries.size() - 1, 0));
    }

    public static <T> TimeSeries of(List<AnnotatedSession<T>> sessions, TimeScale time_scale) {
        List<List<T>> series = new ArrayList<>();

        for (AnnotatedSession<T> session : sessions)
            series.add(time_scale.of(session));

        return new TimeSeries(series, time_scale);
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public int time(int index) {
        return time_scale.time(index);
    }

    public int size() {
        return entries.size();
    }

    public Entry get(int index) {
        return entries.get(index);
    }

    @Override
    public Iterator<TimeSeries<T>.Entry> iterator() {
        return entries.iterator();
    }
}
