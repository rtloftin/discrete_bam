package bam.human.analysis;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public interface TimeScale {

    static TimeScale actions() {
        return new TimeScale() {
            @Override
            public <T> List<T> of(AnnotatedSession<T> session) {
                ArrayList<T> values = new ArrayList<>();

                for(int i=0; i < session.size(); ++i) {
                    String type = session.event(i).getString("type");

                    if(type.equals("take-action") || type.equals("get-action")) {
                        values.add(session.annotation(i));
                    }
                }

                return values;
            }

            @Override
            public int time(int index) {
                return index + 1;
            }
        };
    }

    static TimeScale demonstrations() {
        return new TimeScale() {
            @Override
            public <T> List<T> of(AnnotatedSession<T> session) {
                ArrayList<T> values = new ArrayList<>();
                boolean is_demonstration = false;

                for(int i=0; i < session.size(); ++i) {
                    JSONObject event = session.event(i);
                    String type = event.getString("type");

                    if(type.equals("take-action")) {
                        is_demonstration = event.getJSONObject("data").getBoolean("on-task");
                    } else {
                        if(is_demonstration && type.equals("integrate"))
                            values.add(session.annotation(i));

                        is_demonstration = false;
                    }
                }

                return values;
            }

            @Override
            public int time(int index) {
                return index + 1;
            }
        };
    }

    static TimeScale episodes() {
        return new TimeScale() {
            @Override
            public <T> List<T> of(AnnotatedSession<T> session) {
                ArrayList<T> values = new ArrayList<>();
                boolean is_episode = false;

                for(int i=0; i < session.size(); ++i) {
                    String type = session.event(i).getString("type");

                    if(type.equals("take-action") || type.equals("get-action")) {
                        is_episode = true;
                    } else {
                        if(is_episode && type.equals("integrate"))
                            values.add(session.annotation(i));

                        is_episode = false;
                    }
                }

                return values;
            }

            @Override
            public int time(int index) {
                return index + 1;
            }
        };
    }

    static TimeScale clock(int increment) {
        return new TimeScale() {
            @Override
            public <T> List<T> of(AnnotatedSession<T> session) {
                ArrayList<T> values = new ArrayList<>();

                // Check if the event list is empty
                if(session.size() <= 0)
                    return values;

                // Get the first entry and get the initial time and performance
                T current_annotation = session.annotation(0);
                long start_time = session.event(0).getLong("timestamp");

                // Initialize the time index
                long time_index = increment;

                // Loop over all events in order
                for(int index = 1; index < session.size(); ++index) {

                    // Check if we have reached a new time increment
                    long current_time = (session.event(index).getLong("timestamp") - start_time) / 1000000L;

                    if(current_time >= time_index) {

                        // Fill all time indexes until we reach the most recent event
                        do {

                            // Add data point for current time index
                            values.add(current_annotation);

                            // Increment the time index
                            time_index += increment;
                        } while(current_time >= time_index);
                    }

                    // Update the most recent performance value
                    current_annotation = session.annotation(index);
                }

                // Add the entry for the final time index
                values.add(current_annotation);

                // Return the behavior series
                return values;
            }

            @Override
            public int time(int index) {
                return increment * (index + 1);
            }
        };
    }

    <T> List<T> of(AnnotatedSession<T> session);

    int time(int index);
}
