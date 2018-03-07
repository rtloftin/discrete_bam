package bam.human;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a set of users connected to the server, and
 * the configuration for new users that we wish to add. This
 * class also allows us to limit the number
 */
public class UserGroup {

    /**
     * Represents a connection to a single user. When instantiated,
     * this class will create a unique root directory  for results
     * from this user.  Each user is associated with a single
     * websocket connection, and when that connection closes for
     * any reason, the user will no longer be active
     */
    private class User {

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
            log.write("Client: " + message.optString("message", "blank entry"));
        }

        // Client error message
        private void onErrorMessage(JSONObject message) {

        }

        // Session start message
        private void onSessionStartMessage(JSONObject message) {
            if(null != session)
                throw new RuntimeException("Previous session still active"); // REALLY NOT HOW WE WANT TO HANDLE THIS

            session = session_factory.build(message, (JSONObject msg) -> {
                try {
                    WebSockets.sendText(msg.toString(), channel, null);
                } catch(Exception e) {}
            });
        }

        // Session end message
        private void onSessionEndMessage(JSONObject message) {

        }

        private User(WebSocketChannel channel, File root) throws Exception {
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

    /**
     * A special exception type to be thrown when a user
     * cannot be added because the limit on the number
     * of users has been reached.
     */
    public static class BusyException extends Exception {
        private BusyException() { super("There are currently too many users"); }
    }

    /**
     * A builder class used to configure user
     * groups. Defines the maximum number of
     * users, the root data directory, and the
     * session factory for all users.
     */
    public static class Builder {
        private int max_users = 4;
        private File data_root = null;
        private Session.Factory session_factory = null;

        private Builder() {}

        public Builder maxUsers(int max_users) {
            this.max_users = max_users;

            return  this;
        }

        public Builder dataRoot(File data_root) {
            this.data_root = data_root;

            return this;
        }

        public Builder sessionFactory(Session.Factory session_factory) {
            this.session_factory = session_factory;

            return this;
        }

        public UserGroup build() {
            if(null == data_root || !data_root.isDirectory())
                throw new RuntimeException("No root directory defined for user data");

            return new UserGroup(this);
        }
    }

    public static Builder builder() { return new Builder(); }

    // Configuration
    private final int max_users;
    private final File data_root;
    private final Session.Factory session_factory;

    // The list of active users
    private final List<User> users;

    private UserGroup(Builder builder) {
        this.max_users = builder.max_users;
        this.data_root = builder.data_root;
        this.session_factory = builder.session_factory;

        users = new LinkedList<>();
    }

    public void add(WebSocketChannel channel) throws Exception {

        // Check if we have too many users already
        if(users.size() >= max_users)
            throw new BusyException();

        // Get user root directory
        int id = 1;
        File user_root;

        do {
            user_root = new File(data_root, "user_" + id + File.separator);
            ++id;
        } while(user_root.exists());

        user_root.mkdirs();

        // Add user
        users.add(this.new User(channel, user_root));
    }
}
