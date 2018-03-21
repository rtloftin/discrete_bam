package bam.human;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintStream;

public class Session {

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

    private void record(String type, JSONObject data, JSONObject response) throws JSONException {
        JSONObject event = new JSONObject()
                .put("timestamp", System.nanoTime())
                .put("type", type);

        if(null != data)
            event.put("data", data);

        if(null != response)
            event.put("response", response);

        events.put(event);
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

                remote.takeAction(message.data().getJSONObject("action"));
                JSONObject response = new JSONObject()
                        .put("state", remote.getState())
                        .put("layout", remote.getLayout());

                record("take-action", message.data(), response);

                message.capture();
                message.respond(response);
            } catch(JSONException e) { debug.write("ERROR: json exception"); }
        }).add("get-action", (Connection.Message message) -> {
            try {
                debug.write("agent action");

                remote.takeAction();
                JSONObject response = new JSONObject()
                        .put("state", remote.getState())
                        .put("layout", remote.getLayout());

                record("get-action", null, response);

                message.capture();
                message.respond(response);
            } catch(JSONException e) { debug.write("ERROR: json exception"); }
        }).add("task", (Connection.Message message) -> {
            try {
                debug.write("change task");

                remote.setTask(message.data());
                JSONObject response = new JSONObject()
                        .put("state", remote.getState())
                        .put("layout", remote.getLayout());

                record("task", message.data(), response);

                message.capture();
                message.respond(response);
            } catch(JSONException e) { debug.write("ERROR: json exception"); }
        }).add("update", (Connection.Message message) -> {
            try {
                debug.write("update");

                remote.integrate();

                record("integrate", null, null); // Probably a better way to do implement this

                message.capture();
                message.respond(new JSONObject());
            } catch(JSONException e) { debug.write("ERROR: json exception"); }
        }).add("reset", (Connection.Message message) -> {
            try {
                debug.write("reset");

                remote.resetState();
                JSONObject response = new JSONObject()
                        .put("state", remote.getState())
                        .put("layout", remote.getLayout());

                record("reset", null, response);

                message.capture();
                message.respond(response);
            } catch(JSONException e) { debug.write("ERROR: json exception"); }
        });

        debug.write("session started");

        JSONObject response = new JSONObject()
                .put("state", remote.getState())
                .put("layout", remote.getLayout())
                .put("tasks", remote.getTasks())
                .put("depth", remote.getDepth());

        record("start", null, response);

        return response;
    }

    public void end() throws IOException, JSONException {

        // Detach all event listeners
        listener.remove();

        // Do final data integration

        // Save event log
        PrintStream data = new PrintStream(directory.stream("events"));
        data.print(events.toString(4));
        data.close();

        // Close log
        debug.write("Session Ended");
        debug.close();
    }
}
