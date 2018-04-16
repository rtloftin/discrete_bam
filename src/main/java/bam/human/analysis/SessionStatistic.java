package bam.human.analysis;

import bam.algorithms.Behavior;
import bam.domains.RandomBehavior;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public interface SessionStatistic<T> {

    static SessionStatistic<Integer> demonstrations() {
        return (SessionRecord session) -> {
            int demonstrations = 0;
            boolean is_demonstration = false;

            for(JSONObject event : session.events) {
                String type = event.getString("type");

                if(type.equals("take-action")) {
                    is_demonstration = event.getJSONObject("data").getBoolean("on-task");
                } else if(is_demonstration && type.equals("integrate")) {
                    ++demonstrations;
                    is_demonstration = false;
                }
            }

            return demonstrations;
        };
    }

    static SessionStatistic<Integer> actions() {
        return (SessionRecord session) -> {
            int actions = 0;

            for(JSONObject event : session.events) {
                String type = event.getString("type");

                if(type.equals("get-action") || type.equals("take-action"))
                    ++actions;
            }

            return actions;
        };
    }

    static SessionStatistic<Set<String>> tasks() {
        return (SessionRecord session) -> {
            HashSet<String> tasks = new HashSet<>();
            String current_task = null;

            for(JSONObject event : session.events) {
                String type = event.getString("type");

                if(type.equals("start")) {
                    current_task = event.getJSONObject("response").getJSONObject("layout")
                            .getJSONObject("task").getString("name");
                } else if(type.equals("task")) {
                    current_task = event.getJSONObject("data").getString("name");
                } else if(type.equals("take-action")) {
                    if(null != current_task && event.getJSONObject("data").getBoolean("on-task"))
                        tasks.add(current_task);
                }
            }

            return tasks;
        };
    }

    static SessionStatistic<Performance> performance(Performance.Evaluation evaluation) {
        return (SessionRecord session) -> {
            Behavior current_behavior = Behavior.get();

            for(JSONObject event : session.events)
                if(event.getString("type").equals("integrate"))
                    current_behavior = Behavior.load(event.getJSONObject("behavior"));

            return evaluation.of(current_behavior);
        };
    }

    T of(SessionRecord session);

    default List<T> of(SessionRecords records) {
        List<T> statistics = new ArrayList<>();

        for(SessionRecord record : records)
            statistics.add(of(record));

        return statistics;
    }
}
