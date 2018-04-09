package bam.human.analysis;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Sequence<T> {

    private final List<JSONObject> events;
    private final List<T> performance;

    public final HashSet<String> tasks;

    private Sequence() {
        this.events = new ArrayList<>();
        this.performance = new ArrayList<>();

        tasks = new HashSet<>();
    }

    public static <T> Sequence<T> start() {
        return new Sequence<>();
    }

    public int size() {
        return events.size();
    }

    public JSONObject event(int index) {
        return events.get(index);
    }

    public void add(JSONObject event, T performance) {
        this.events.add(event);
        this.performance.add(performance);

        if(event.getString("type").equals("task"))
            tasks.add(event.getJSONObject("data").getString("name"));
    }

    public T performance(int index) {
        return performance.get(index);
    }

    public T performance() {
        return performance.get(performance.size() - 1);
    }
}
