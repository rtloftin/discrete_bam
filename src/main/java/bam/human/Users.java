package bam.human;

import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a set of users connected to the server, and
 * the configuration for new users that we wish to add. This
 * class also allows us to limit the number of users in the system
 */
class Users {

    /**
     * Represents a connection to a single user. When instantiated,
     * this class will create a unique root directory  for results
     * from this user.  Each user is associated with a single
     * websocket connection, and when that connection closes for
     * any reason, the user will no longer be active
     */
    private class User {

        // The user's global log file
        private Log log;

        // The active session for this user
        private Session current_session = null;

        private User(Connection connection, Directory directory) throws Exception {

            // Create and initialize log log
            log = Log.create(directory.stream("log"));
            log.write("Log started for user");

            // Log client message
            connection.listen().add("log", (Connection.Message message) -> {
                log.write("CLIENT: " + message.data().optString("text", "no log message"));
            }).add("error", (Connection.Message message) -> {
                log.write("ERROR-CLIENT: " + message.data().optString("text", "no error message"));
                message.error("client error");
            }).add("start-session", (Connection.Message message) -> {
                if(null != current_session) {
                    log.write("ERROR: previous session not closed");
                    message.error("server error");
                } else {
                    try {
                        current_session = sessions.build(connection, directory.unique("sessions"), message.data());
                        message.respond(current_session.start());

                        log.write("SESSION: started new session");
                    } catch (Exception e) {
                        log.write("ERROR: could not start new session - " + e.getMessage());
                        message.error("server error");
                    }
                }
            }).add("end-session", (Connection.Message message) -> {
                if(null != current_session) {
                    current_session.end("finished");
                    current_session = null;

                    message.respond();

                    log.write("SESSION: user ended session");
                }
            }).add("complete", (Connection.Message message) -> {
                connection.close("complete");
            });

            connection.open((String reason) -> {
                if(null != current_session)
                    current_session.end(reason);

                log.write("CONNECTION CLOSED: " + reason);
                log.close();

                users.remove(this);
            });
        }
    }


    /**
     * A builder class used to configure user
     * groups. Defines the maximum number of
     * users, the root data directory, and the
     * session factory for all users.
     */
    static class Builder {
        private int max_users = 4;
        private Directory directory = null;
        private Session.Factory sessions = null;

        private Builder() {}

        public Builder maxUsers(int max_users) {
            this.max_users = max_users;
            return  this;
        }

        public Builder dataRoot(Directory directory) {
            this.directory = directory;
            return this;
        }

        public Builder sessions(Session.Factory sessions) {
            this.sessions = sessions;
            return this;
        }

        public Users build() {
            if(null == directory)
                throw new RuntimeException("No root directory defined for user data");
            if(null == sessions)
                throw new RuntimeException("No session factory defined");

            return new Users(this);
        }
    }

    static Builder builder() { return new Builder(); }

    // Configuration
    private final int max_users;
    private final Directory directory;
    private final Session.Factory sessions;

    // The list of active users
    private final List<User> users;

    private Users(Builder builder) {
        this.max_users = builder.max_users;
        this.directory = builder.directory;
        this.sessions = builder.sessions;

        users = new LinkedList<>();
    }

    void add(Connection connection) {

        // Check if we have too many users already
        if(users.size() >= max_users)
            connection.refuse();

        // Try to create the user
        try {
            users.add(this.new User(connection, directory.unique("users")));
        } catch(Exception e) {
            connection.decline();
        }
    }
}
