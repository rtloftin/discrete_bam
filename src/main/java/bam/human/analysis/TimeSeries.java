package bam.human.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of time-series
 * for a set of sessions, with a common time
 * scale such as actions, demonstrations or
 * wall-clock time.
 */
public class TimeSeries<T> {

    public static class Entry<T> {
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

    private final List<Entry<T>> entries;
    private final List<T> performance;
    private final List<Integer> time;

    private final int start;
    private final int end;

    private TimeSeries(List<List<T>> series, TimeScale scale) {
        entries = new ArrayList<>();
        performance = new ArrayList<>();
        time = new ArrayList<>();

        for(List<T> sequence : series) {
            for(int i=0; i < sequence.size(); ++i) {
                if(entries.size() <= i)
                    entries.add(new Entry<>(scale.time(i)));

                entries.get(i).add(sequence.get(i));
            }

            time.add(sequence.size());
            performance.add(sequence.get(sequence.size() - 1));
        }

        start = scale.time(0);
        end = scale.time(Math.max(entries.size() - 1, 0));
    }

    public static <T> TimeSeries of(Sequences<T> sequences, TimeScale scale) {
        List<List<T>> series = new ArrayList<>();

        for(int i=0; i < sequences.size(); ++i)
            series.add(scale.process(sequences.get(i)));

        return new TimeSeries(series, scale);
    }

    public int size() {
        return entries.size();
    }

    public Entry<T> get(int index) {
        return entries.get(index);
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }

    public List<T> performance() {
        return performance;
    }

    public List<Integer> time() {
        return time;
    }
}
