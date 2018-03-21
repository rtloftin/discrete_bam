package bam.human;

import java.io.IOException;

/**
 * Represents a connection to a single user. When instantiated,
 * this class will create a unique root directory  for results
 * from this user.  Each user is associated with a single
 * websocket connection, and when that connection closes for
 * any reason, the user will no longer be active
 */
public class User {

    // The user group this user belongs to
    private Users user_group;

    // The websocket connection associated with this user
    private Connection connection;

    // The user's root data directory
    private Directory directory;

    // The user's global log file
    private Log log;

    // The active session for this user
    private OldSession current_session = null;

    private User(Connection connection, Directory directory, Users user_group) throws IOException {
        this.connection = connection;
        this.directory = directory;
        this.user_group = user_group;

        // Create and initialize log log
        log = Log.create(directory.stream("log"));
        log.write("Log started for user");

        // Log client message
        connection.listen().add("log", (Connection.Message message) -> {
            log.write("CLIENT: " + message.data().optString("text", "no log message"));
        }).add("error", (Connection.Message message) -> {
            log.write("ERROR-CLIENT: " + message.data().optString("text", "no error message"));
            disconnect();
        }).add("finished", (Connection.Message message) -> {
            log.write("FINISHED: disconnecting");
            disconnect();
        }).add("start-session", this::startSession);

        // Open connection
        connection.open(this::disconnect);
    }

    // Cleans up the previous session if one is active
    private void endSession() {
        if(null != current_session)
            current_session.end();
    }

    // Starts a new session
    private void startSession(Connection.Message message) {

        // End the current session
        endSession();

        // Try to start a new session
        try {
            current_session = user_group.session(connection, directory.unique("sessions"), message);
            log.write("SESSION: started new session");
        } catch (Exception e) {
            System.out.println("Error starting session: " + e.getMessage());
            log.write("ERROR: could not start session");
        }
    }

    private void disconnect() {

        // Close the channel if it isn't already closed
        connection.close();

        // End the current session
        endSession();

        // Close log
        log.close();

        // Remove this user from the user group
        user_group.remove(this);
    }

    // Really need to work on error handling, the server should never crash on a checked exception
    static User with(Connection connection, Directory directory, Users user_group) throws IOException {
        return new User(connection, directory, user_group);
    }
}
