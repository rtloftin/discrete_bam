package bam.human;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Consumer;

/**
 * This interface represents a connection to a client,
 * without defining the specific protocol used to communicate
 * with that client.  This object allows for listeners
 * to be added and removed for different "types" of
 * messages, and allows listeners to respond to or simply
 * capture specific messages.  Also allows for messages to
 * be pushed to the client, and responses received. The
 * data for each message is represented as a JSON object.
 */
public interface Connection {

    /**
     * Represents a single message received
     * by this connection.
     */
    interface Message {

        /**
         * Gets the data associated with this message.
         *
         * @return a JSON object containing the structured message data
         */
        JSONObject data();

        /**
         * Captures the message so that no subsequent
         * message handlers are called for it.
         */
        void capture();

        /**
         * Responds to the message.  Does nothing
         * if a previous handler has already
         * provided a response.
         *
         * @param response the data for the message response
         * @throws JSONException if the response data is not properly formatted
         */
        void respond(JSONObject response) throws JSONException;
    }

    /**
     * Represents a group of message listeners
     * attached to this connection.
     */
    interface Listener {

        /**
         * Adds a new listener to the connection that
         * handles messages of the type provided.
         *
         * @param type the type of messages to listen for
         * @param handler the message handler
         * @return the same Listener object, for chaining
         */
        Listener add(String type, Consumer<Message> handler);

        /**
         * Removes all the listeners added through this
         * object from the connection.
         */
        void remove();
    }

    /**
     * Opens the connection on both ends.  Before this
     * is called, efforts to send messages on either
     * side should fail.  Does nothing if the connection
     * is already open or has been closed, refused or declined.
     *
     * @param on_close a callback
     */
    void open(Runnable on_close);

    /**
     * Indicates that the server encountered an unrecoverable
     * error when trying to connect the user.  Does nothing
     * if the connection has already been opened.
     */
    void decline();

    /**
     * Explicitly refuses the connection, indicating that
     * this end of the connection is busy, or otherwise
     * unwilling to process the request.  Does nothing
     * if the connection has already been opened.
     */
    void refuse();

    /**
     * Permanently closes the connection on both ends.  Does
     * nothing if the connection is already closed, or has
     * not been opened.
     */
    void close();

    /**
     * Creates a new listener object attached to this
     * connection, to which individual handlers may be added.
     *
     * @return a new Listener object for this connection
     */
    Listener listen();

    /**
     * Sends a message, with a callback
     * provided for a possible response.
     *
     * @param type the type of the message
     * @param data the message data
     * @param callback the callback to handle the response to this message
     * @throws JSONException if the message data is not properly formatted
     */
    void send(String type, JSONObject data, Consumer<JSONObject> callback) throws JSONException;
}
