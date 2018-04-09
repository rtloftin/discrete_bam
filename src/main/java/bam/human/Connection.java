package bam.human;

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
     * by this connection.  This message is
     * passed to each of the message handlers
     * for the given message type.  The response
     * or error message that is provided first
     * is the one that gets sent to the client.
     */
    interface Message {

        /**
         * Gets the data associated with this message.
         *
         * @return a JSON object containing the structured message data
         */
        JSONObject data();

        /**
         * Responds to the message.  Does nothing if
         * a response has already been provided.
         *
         * @param response the data for the message response
         */
        void respond(JSONObject response);

        /**
         * Responds to the message.  Does nothing
         * if a previous handler has already
         * provided a response.
         */
        default void respond() {
            respond(new JSONObject());
        }

        /**
         * Indicates that there was an error handing this
         * message. Does nothing if a response has already
         * been provided.
         *
         * @param error the specific error message
         */
        void error(String error);
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
     * Accepts a callback which is called when the connection is closed externally.
     * The callback accepts a string indicating why the connection was closed.
     *
     * @param on_close the callback for when the connection fails or is closed by the client
     */
    void open(Consumer<String> on_close);

    /**
     * Explicitly refuses to open the connection, providing
     * an error message to return to the client, then closing
     * the connection itself.
     *
     * @param reason the reason the connection is being refused
     */
    void refuse(String reason);

    /**
     * Permanently closes the connection, without calling
     * the on_close callback provided when the connection
     * was opened.  Does nothing if the connection is closed.
     */
    void close();

    /**
     * Creates a new listener object attached to this
     * connection, to which individual handlers may be added.
     *
     * @return a new Listener object for this connection
     */
    Listener listen();
}
