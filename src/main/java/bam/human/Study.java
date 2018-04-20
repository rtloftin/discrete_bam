package bam.human;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;

/**
 * Represents a set of users connected to the server, and
 * the configuration for new users that we wish to add. This
 * class also allows us to limit the number of users in the system
 */
class Study {

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

        private User(Connection connection, Directory directory, String code) throws Exception {

            // Save verification code
            new PrintStream(directory.stream("code")).append(code).close();

            // Create and initialize log log
            log = Log.create(directory.stream("log"));
            log.write("Log started for user");
            log.write("Verification code: " + code);

            // Log client message
            connection.listen().add("log", (Connection.Message message) -> {
                log.write("LOG-CLIENT: " + message.data().optString("text", "no log message"));
            }).add("error", (Connection.Message message) -> {
                log.write("ERROR-CLIENT: " + message.data().optString("text", "no error message"));
            }).add("code", (Connection.Message message) -> {
                log.write("CODE REQUESTED");

                try {
                    message.respond(new JSONObject().put("value", code));
                } catch(JSONException e) {
                    log.write("ERROR: " + e.getMessage());
                }
            }).add("start-session", (Connection.Message message) -> {
                if(null != current_session) {
                    log.write("ERROR: previous session not closed");
                    message.error("server error");
                } else {
                    try {
                        current_session = sessions.build(connection, directory.unique("sessions"), message.data());
                        message.respond(current_session.start());

                        log.write("SESSION: started new session");
                    } catch(Exception e) {
                        log.write("ERROR: could not start new session - " + e.getMessage());
                        message.error("server error");
                    }
                }
            }).add("end-session", (Connection.Message message) -> {
                if(null != current_session) {
                    try {
                        current_session.end("finished");
                        current_session = null;

                        message.respond();
                        log.write("SESSION: user ended session");
                    } catch(Exception e) {
                        log.write("ERROR: could not end session - " + e.getMessage());
                        message.error("server error");
                    }
                }
            }).add("complete", (Connection.Message message) -> {
                log.write("CLIENT COMPLETE");
            });

            connection.open((String reason) -> {
                if(null != current_session) {
                    try {
                        current_session.end(reason);
                    } catch(Exception e) {
                        log.write("ERROR: could not end session - " + e.getMessage());
                    }
                }

                log.write("CONNECTION CLOSED: " + reason);
                log.close();

                pool.remove(this);
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
        private Pool pool;
        private Directory directory = null;
        private Session.Factory sessions = null;
        private CodeFactory codes = null;

        private Builder() {}

        public Builder pool(Pool pool) {
            this.pool = pool;
            return this;
        }

        public Builder dataRoot(Directory directory) {
            this.directory = directory;
            return this;
        }

        public Builder sessions(Session.Factory sessions) {
            this.sessions = sessions;
            return this;
        }

        public Builder codes(CodeFactory codes) {
            this.codes = codes;
            return this;
        }

        public Study build() throws IOException {
            if(null == directory)
                throw new RuntimeException("No root directory defined for user data");
            if(null == sessions)
                throw new RuntimeException("No session factory defined");
            if(null == codes)
                codes = CodeFactory.dummy("no verification codes defined");

            return new Study(this);
        }
    }

    static Builder builder() { return new Builder(); }

    // Configuration
    private final Pool pool;
    private final Directory directory;
    private final Session.Factory sessions;
    private final CodeFactory codes;

    private Log log;

    private Study(Builder builder) {
        this.pool = builder.pool;
        this.directory = builder.directory;
        this.sessions = builder.sessions;
        this.codes = builder.codes;

        // Start study-wide log
        try {
            log = Log.create(directory.stream("codes"));
        } catch(IOException e) {
            log = Log.dummy();
        }

        log.write("log started");
    }

    void add(Connection connection){

        // Check if we have too many users already
        if(pool.full()) {
            connection.refuse("busy");
            log.write("refuse user, too busy");
        }

        // Get the next verification code
        Optional<String> code = codes.nextCode();

        if(code.isPresent()) {
            try {
                pool.add(this.new User(connection, directory.unique("users"), code.get()));
                log.write("added user, code: " + code.get());
            } catch (Exception e) { // What kinds of exceptions can occur here?
                connection.refuse(e.getMessage());
                log.write("refused user, error: " + e.getMessage());
            }
        } else {
            connection.refuse("codes exhausted");
            log.write("refused user, codes exhausted");
        }
    }
}
