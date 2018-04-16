package bam.human;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

public class Session {

    /**
     * An interface for objects that construct sessions based
     * on a configuration message provided by the client.
     */
    public interface Factory {

        /**
         * Constructs a new session object based on an
         * initialization message provided by the client.
         *
         * @param connection the connection to the client
         * @param directory the directory where session data will be saved
         * @param config the configuration and initial state session
         * @return the new session object
         * @throws JSONException if the initial message is not properly formatted
         */
        Session build(Connection connection, Directory directory, JSONObject config) throws IOException, JSONException;
    }

    // The remote simulation associated with this session
    private Remote remote;

    // The root directory for this
    private Directory directory;

    // The message listeners associated with this session
    private Connection.Listener listener;

    // The event log for the entire session, basically one large JSON array
    private JSONArray events;

    // A log file for this session that is flushed at every update, used for debugging mostly
    private Log debug;

    private JSONObject record(String type) throws JSONException {
        JSONObject event = new JSONObject()
                .put("timestamp", System.nanoTime())
                .put("type", type);

        events.put(event);

        return event;
    }

    private Session(Remote remote, Connection connection, Directory directory) throws IOException {
        this.remote = remote;
        this.directory = directory;

        // Get connection listener
        listener = connection.listen();

        // Initialize event log
        events = new JSONArray();

        // Start session log
        debug = Log.create(directory.stream("debug"))
                .write("Session Initialized");
    }

    public static Session build(Remote remote, Connection connection, Directory directory) throws IOException {
        return new Session(remote, connection, directory);
    }

    public JSONObject start() throws JSONException {

        // Attach event handlers
        listener.add("take-action", (Connection.Message message) -> {
            try {
                debug.write("user action");

                remote.takeAction(message.data());
                JSONObject response = new JSONObject()
                        .put("state", remote.getState())
                        .put("layout", remote.getLayout());

                record("take-action")
                        .put("data", message.data())
                        .put("state", response.getJSONObject("state"));

                message.respond(response);
            } catch(JSONException e) {
                debug.write("ERROR: json exception");
                message.error("json error");
            }
        }).add("get-action", (Connection.Message message) -> {
            try {
                debug.write("agent action");

                remote.takeAction();
                JSONObject response = new JSONObject()
                        .put("state", remote.getState())
                        .put("layout", remote.getLayout());

                record("get-action")
                        .put("state", response.getJSONObject("state"));

                message.respond(response);
            } catch(JSONException e) {
                debug.write("ERROR: json exception");
                message.error("json error");
            }
        }).add("task", (Connection.Message message) -> {
            try {
                debug.write("change task");

                remote.setTask(message.data());
                JSONObject response = new JSONObject()
                        .put("state", remote.getState())
                        .put("layout", remote.getLayout());

                record("task")
                        .put("data", message.data())
                        .put("state", response.getJSONObject("state"))
                        .put("layout", response.getJSONObject("layout"));

                message.respond(response);
            } catch(JSONException e) {
                debug.write("ERROR: json exception");
                message.error("json error");
            }
        }).add("update", (Connection.Message message) -> {
            try {
                debug.write("update");

                record("integrate")
                        .put("behavior", remote.integrate());

                message.respond(new JSONObject());
            } catch(JSONException e) {
                debug.write("ERROR: json exception");
                message.error("json error");
            }
        }).add("reset", (Connection.Message message) -> {
            try {
                debug.write("reset");

                remote.resetState();
                JSONObject response = new JSONObject()
                        .put("state", remote.getState())
                        .put("layout", remote.getLayout());

                record("reset")
                        .put("state", response.getJSONObject("state"));

                message.respond(response);
            } catch(JSONException e) {
                debug.write("ERROR: json exception");
                message.error("json error");
            }
        }).add("set-state", (Connection.Message message) -> {
            try {
                debug.write("set state");

                remote.setState(message.data());
                JSONObject response = new JSONObject()
                        .put("state", remote.getState())
                        .put("layout", remote.getLayout());

                record("set-state")
                        .put("data", message.data())
                        .put("state", response.getJSONObject("state"));

                message.respond(response);
            } catch(JSONException e) {
                debug.write("ERROR: json exception");
                message.error("json error");
            }
        });

        debug.write("session started");

        JSONObject response = new JSONObject()
                .put("state", remote.getState())
                .put("layout", remote.getLayout())
                .put("tasks", remote.getTasks())
                .put("depth", remote.getDepth());

        record("start")
                .put("response", response);

        return response;
    }

    /**
     * Terminates the session and saves all the session
     * data.  Allows for a reason for terminating this
     * session to be recorded, that is, whether there was
     * an error, or whether the user actually ended the
     * session on purpose.
     *
     * @param reason the session was ended
     */
    public void end(String reason) {

        // Detach all event listeners
        listener.remove();

        try {

            // Do final data integration -- we do this already on the client side
            /* record("integrate").put("behavior", remote.integrate()); */

            // Record end event
            record("end")
                    .put("reason", reason);

            // Compress and save event log
            CompressorOutputStream stream = new CompressorStreamFactory()
                    .createCompressorOutputStream(CompressorStreamFactory.GZIP, directory.stream("events"));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));

            events.write(writer);
            writer.close();
        } catch(Exception e) {
            debug.write("ERROR: couldn't save session data");
        }

        // Close log
        debug.write("Session Ended");
        debug.close();
    }
}
