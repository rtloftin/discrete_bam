package bam.human;

import io.undertow.websockets.core.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class WebsocketConnection extends AbstractReceiveListener implements Connection {

    private static class Message implements Connection.Message {

        private final String type;
        private final int id;
        private final JSONObject data;

        private JSONObject response = null;
        private String error = null;


        private Message(JSONObject message) throws JSONException {
            type = message.getString("type");
            id = message.getInt("id");
            data = message.getJSONObject("data");
        }

        @Override
        public JSONObject data() {
            return data;
        }

        @Override
        public void respond(JSONObject response) {
            if(null == this.response)
                this.response = response;
        }

        @Override
        public void error(String error) {
            if(null != this.error)
                this.error = error;
        }
    }

    // Connection status codes
    private static final int PENDING = 0;
    private static final int OPEN = 1;
    private static final int CLOSED = 2;

    // The current connection status
    private int status = PENDING;

    // The message handlers
    private Map<String, List<Consumer<Connection.Message>>> handlers = new HashMap<>();

    // The connection close callback
    private Consumer<String> on_close = null;

    // Session timeout
    private long timeout;
    private Timer timer = null;

    // The actual websocket
    private WebSocketChannel channel;

    private WebsocketConnection(WebSocketChannel channel, long timeout) {
        this.channel = channel;
        this.timeout = timeout;
    }

    public static WebsocketConnection with(WebSocketChannel channel, long timeout) {
        return new WebsocketConnection(channel, timeout);
    }

    @Override
    public void open(Consumer<String> on_close) {
        if(PENDING == status) {

            // Set close callback
            this.on_close = on_close;

            // Respond ready
            try {

                // Open channel
                channel.getReceiveSetter().set(this);
                channel.resumeReceives();

                // Set status to open
                status = OPEN;

                // Set timeout
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(null != on_close)
                            on_close.accept("timeout");

                        close();
                    }
                }, timeout);

                // Respond ready
                WebSockets.sendText(new JSONObject()
                        .put("ready", "true").toString(4), channel, null);
            } catch(JSONException e) {
                close();
            }
        }
    }

    @Override
    public void refuse(String reason) {
        if(PENDING == status) {

            // Try to send a response
            try {
                WebSockets.sendText(new JSONObject()
                        .put("error", reason).toString(4), channel, null);
            } catch(JSONException e) { /* We tried, nothing left to do */ }

            close();
        }
    }

    @Override
    public void close() {
        if(CLOSED != status) {
            status = CLOSED;

            if(null != timer)
                timer.cancel();

            if(channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) { /* We tried, move on */ }
            }
        }
    }

    @Override
    public Listener listen() {
        List<Map.Entry<String, Consumer<Connection.Message>>> listeners = new LinkedList<>();

        return new Listener() {

            @Override
            public Listener add(String type, Consumer<Connection.Message> handler) {
                listeners.add(new AbstractMap.SimpleEntry<>(type, handler));

                if(!handlers.containsKey(type))
                    handlers.put(type, new LinkedList<>());

                handlers.get(type).add(handler);

                return this;
            }

            @Override
            public void remove() {
                for(Map.Entry<String, Consumer<Connection.Message>> entry : listeners)
                    handlers.get(entry.getKey()).remove(entry.getValue());
            }
        };
    }

    @Override
    protected synchronized void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
        try {
            Message msg = new Message(new JSONObject(message.getData()));

            if(handlers.containsKey(msg.type))
                for(Consumer<Connection.Message> handler : handlers.get(msg.type))
                    handler.accept(msg);

            JSONObject response = new JSONObject().put("callback", msg.id);

            if(null != msg.response)
                response.put("data", msg.response);
            else if(null != msg.error)
                response.put("error", msg.error);
            else
                response.put("data", new JSONObject());

            WebSockets.sendText(response.toString(4), channel, null);
        } catch(JSONException e) { /* We tried, nothing to be done */ }
    }

    @Override
    protected synchronized  void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) {

        // Call close handler
        on_close.accept("connection closed");

        // Close connection
        close();
    }

    @Override
    protected synchronized void onError(WebSocketChannel channel, Throwable error) {

        // Call close handler
        on_close.accept("connection error: " + error.getMessage());

        // Close connection
        close();
    }
}
