package bam.human;

import org.json.JSONException;

/**
 * This class represents a combination of a single learning agent
 * and an environment, that is, it represents a learning
 * session.  Each session has a single incoming and outgoing
 * message handler, and has a root data directory provided by
 * when it is created
 *
 * It isn't really clear what the session object does anymore.  Originally it
 * contained a callback for handling messages, but now we can attach message
 * handlers directly to a connection.  Sessions now largely serve as a way
 * to close these connections, and any processes or file handles associated
 * with them.  It also seems that all sessions are basically the same, the only
 * different between is reflected in the Remote object.  A better
 * option, instead of having this interface, would be to use a factory interface
 * for the environments, and have a single session object which takes a connection
 * and an environment for its constructor.
 *
 */
public interface OldSession {

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
         * @param config initial message that started the session
         * @return the new session object
         * @throws JSONException if the initial message is not properly formatted
         */
        OldSession build(Connection connection, Directory directory, Connection.Message config) throws JSONException;
    }

    /**
     * Ends this session. Saves any remaining data and closes
     * any open file handles.  Also removes any connection
     * listeners this session created.
     */
    void end();
}
