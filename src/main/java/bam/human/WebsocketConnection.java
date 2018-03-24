package bam.human;

import io.undertow.websockets.core.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class WebsocketConnection extends AbstractReceiveListener implements Connection {

    private class Message implements Connection.Message {

        private String type;
        private int id;
        private JSONObject data;

        private boolean is_captured = false;
        private boolean has_responded = false;

        private Message(JSONObject message) throws JSONException {
            type = message.getString("type");
            id = message.getInt("id");
            data = message.getJSONObject("data");
        }

        @Override
        public JSONObject data() { return data; }

        @Override
        public void capture() { is_captured = true; }

        @Override
        public void respond(JSONObject response) {
            if(!has_responded) {
                try {
                    JSONObject message = new JSONObject()
                            .put("response", id)
                            .put("data", response);

                    WebSockets.sendText(message.toString(2), channel, null);

                    has_responded = true;
                } catch(JSONException e) {
                    error("connection error");
                }
            }
        }

        @Override
        public void error(String error) {
            close(error);
        }
    }

    // Connection status codes
    private static final int PENDING = 0;
    private static final int OPEN = 1;
    private static final int CLOSED = 2;

    // The current connection status
    private int status = PENDING;

    // The current message index
    private int index = 0;

    // The message handlers
    private Map<String, List<Consumer<Connection.Message>>> handlers = new HashMap<>();

    // The response callbacks
    private Map<Integer, Consumer<JSONObject>> callbacks = new HashMap<>();

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

            // Set timeout
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    close("timeout");
                }
            }, timeout);

            // Open channel
            channel.getReceiveSetter().set(this);
            channel.resumeReceives();

            // Respond ready
            WebSockets.sendText("ready", channel, null);

            // Update status
            status = OPEN;
        }
    }

    @Override
    public void decline() {
        if(PENDING == status) {

            // Respond that there was an error
            WebSockets.sendText("error", channel, null);

            // Update status
            status = CLOSED;
        }
    }

    @Override
    public void refuse() {
        if(PENDING == status) {

            // Respond busy
            WebSockets.sendText("busy", channel, null);

            // Update status
            status = CLOSED;
        }
    }

    @Override
    public void close(String reason) {
        if(CLOSED != status){

            // Update status
            status = CLOSED;

            // Fire callback
            if(null != on_close)
                on_close.accept(reason);

            // Cancel timeout
            if(null != timer)
                timer.cancel();

            // Try to close the connection - may already be closed
            try {
                channel.close();
            } catch(IOException e) { /* Tried to close, nothing to be done */}
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
    public void send(String type, JSONObject data, Consumer<JSONObject> callback) throws JSONException {
        if(OPEN == status) {
            if (null != callback)
                throw new UnsupportedOperationException(); // callbacks.put(index, callback);

            JSONObject message = new JSONObject()
                    .put("type", type)
                    .put("id", index++)
                    .put("data", data);

            WebSockets.sendText(message.toString(2), channel, null);
        }
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
        try {
            JSONObject json = new JSONObject( message.getData());

            if(json.has("response")) {
                int id = json.getInt("response");

                if(callbacks.containsKey(id)) {
                    callbacks.get(id).accept(json.getJSONObject("data"));
                    callbacks.remove(id);
                }
            } else {
                Message msg = this.new Message(json);

                if(handlers.containsKey(msg.type))
                    for(Consumer<Connection.Message> handler : handlers.get(msg.type))
                        if(!msg.is_captured)
                            handler.accept(msg);
            }
        } catch(JSONException e) {
            close("connection error");
        }
    }

    @Override
    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) {
        close("connection error");
    }
}
