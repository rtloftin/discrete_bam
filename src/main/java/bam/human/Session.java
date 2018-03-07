package bam.human;

import org.json.JSONObject;

import java.util.function.Consumer;

/**
 * Represents a combination of a single learning agent
 * and an environment, that is, it represents a learning
 * session.  Has a single incoming and outgoing message handler.
 *
 * WE MAY EVENTUALLY ADD METHODS FOR LOGGING SESSION DATA, AND FOR ERROR HANDLING
 */
public interface Session {

    /**
     * An interface for objects that construct sessions based
     * on a configuration message provided by the client.
     */
    interface Factory {

        /**
         * Builds a new session object.
         *
         * @param init_message the client message describing the session
         * @param send_message the callback for this session to send messages to the client
         * @return the session object
         */
        Session build(JSONObject init_message, Consumer<JSONObject> send_message);
    }

    /**
     * Handles all incoming client messages for this session.
     *
     * @param message the client message as a JSON object
     */
    void onMessage(JSONObject message);
}
