package bam.human;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Represents a connection to a single user. When instantiated,
 * this class will create a unique root directory  for results
 * from this user.  Each user is associated with a single
 * websocket connection, and when that connection closes for
 * any reason, the user will no longer be active
 */
public class User {

    // The websocket connection associated with this user
    private WebSocketChannel channel;

    // The user's root data directory
    private File root;

    // The user's log file
    private Log log;

    // Handlers for messages that are interpreted outside of a session
    private HashMap<String, Consumer<JSONObject>> handlers;

    // The active session for this user
    private Session session = null;

    // Main message handler
    private void onMessage(String message) {
        try {
            JSONObject msg = new JSONObject(message);
            String type = msg.getString("type");

            if(handlers.containsKey(type))
                handlers.get(type).accept(msg);
            else if(null != session)
                session.onMessage(msg);

        } catch(Exception e) {
            System.out.println("an exception occurred: " + e.getMessage());
        }
    }

    // Client log handler
    private void onLogMessage(JSONObject message) {
        log.write(message.optString("message", "blank client entry"));
    }

    // Client error message
    private void onErrorMessage(JSONObject message) {

    }

    // Session start message
    private void onSessionStartMessage(JSONObject message) {

    }

    // Session end message
    private void onSessionEndMessage(JSONObject message) {

    }

    public User(WebSocketChannel channel, File root) throws Exception {
        this.channel = channel;
        this.root = root;

        // Create log
        log = Log.file(new File(root, "log"));

        // Define message handlers
        handlers = new HashMap<>();
        handlers.put("log", this::onLogMessage);
        handlers.put("error", this::onErrorMessage);
        handlers.put("start-session", this::onSessionStartMessage);
        handlers.put("end-session", this::onSessionEndMessage);

        // Set channel message handler
        channel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel,
                                             BufferedTextMessage message) throws IOException {
                onMessage(message.getData());
            }
        });
    }
}
