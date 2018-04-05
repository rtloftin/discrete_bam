package bam.human.analysis;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single interactive learning session
 * with a single agent in a fixed environment. Sessions
 * are the basic unit of data analysis, that is, we organize
 * individual sessions into conditions, and treat each session
 * as a sample.  Various time series and performance measures
 * can be generated from a session.
 */
public class SessionRecord {

    public final JSONObject algorithm;
    public final JSONObject environment;

    public final List<JSONObject> events;

    private SessionRecord(Path root) throws IOException, JSONException {

        // Get algorithm
        algorithm = new JSONObject(FileUtils.readFileToString(root.resolve("algorithm").toFile(), "UTF-8"));

        // Get environment
        environment = new JSONObject(FileUtils.readFileToString(root.resolve("environment").toFile(), "UTF-8"));

        // Load event list
        JSONArray json_events = new JSONArray(FileUtils.readFileToString(root.resolve("events").toFile(), "UTF-8"));
        events = new ArrayList<>();

        for (int i = 0; i < json_events.length(); ++i)
            events.add(json_events.getJSONObject(i));
    }

    public static SessionRecord load(Path root) throws IOException, JSONException {
        return new SessionRecord(root);
    }
}
