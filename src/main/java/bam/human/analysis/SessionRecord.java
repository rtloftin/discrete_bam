package bam.human.analysis;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public final int participant; // The ID of the participant
    public final int agent; // Which of the seven possible sessions this is

    private SessionRecord(Path root, EventDecoder decoder, int participant) throws IOException, JSONException {

        // Get the participant ID
        this.participant = participant;

        // Get which of the seven possible sessions this is
        this.agent = Integer.parseInt(root.getFileName().toString());

        // Get algorithm
        algorithm = new JSONObject(FileUtils.readFileToString(root.resolve("algorithm").toFile(), "UTF-8"));

        // Get environment
        environment = new JSONObject(FileUtils.readFileToString(root.resolve("environment").toFile(), "UTF-8"));

        // Load event list
        events = decoder.events(Files.newInputStream(root.resolve("events")));
    }

    public static SessionRecord load(Path root, EventDecoder decoder, int participant) throws IOException, JSONException {
        return new SessionRecord(root, decoder, participant);
    }
}
