package bam.human.analysis;

import org.json.JSONException;
import org.json.JSONObject;

public interface SessionFilter {

    static SessionFilter all(SessionFilter... filters) {
        return (SessionRecord session) -> {
            for(SessionFilter filter : filters)
                if(!filter.good(session))
                    return false;

            return true;
        };
    }

    static SessionFilter tutorial() {
        return (SessionRecord session) -> {
            try {
                return !session.algorithm.getString("name").equals("Expert");
            } catch(JSONException e) {
                return false;
            }
        };
    }

    static SessionFilter complete() {
        return (SessionRecord session) -> {
            try {
                JSONObject end = session.events.get(session.events.size() - 1);
                return end.getString("reason").equals("finished");
            } catch(JSONException e) {
                return false;
            }
        };
    }

    static SessionFilter environment(String name) {
        return (SessionRecord session) -> {
            try {
                return session.environment.getString("name").equals(name);
            } catch(JSONException e) {
                return false;
            }
        };
    }

    static SessionFilter algorithm(String name) {
        return (SessionRecord session) -> {
            try {
                return session.algorithm.getString("name").equals(name);
            } catch(JSONException e) {
                return false;
            }
        };
    }

    boolean good(SessionRecord session);
}
