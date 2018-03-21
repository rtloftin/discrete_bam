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
        public void respond(JSONObject response) throws JSONException {
            if(!has_responded) {
                JSONObject message = new JSONObject()
                        .put("response", id)
                        .put("data", response);

                WebSockets.sendText(message.toString(2), channel, null);

                has_responded = true;
            }
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
    private Runnable on_close = null;

    // The actual websocket
    private WebSocketChannel channel;

    private WebsocketConnection(WebSocketChannel channel) { this.channel = channel; }

    public static WebsocketConnection with(WebSocketChannel channel) {
        return new WebsocketConnection(channel);
    }

    @Override
    public void open(Runnable on_close) {
        if(PENDING == status) {

            // Set close callback
            this.on_close = on_close;

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
    public void close() {
        if(CLOSED != status){
            try {
                channel.close();
            } catch(IOException e) { /* Nothing to be done */ }
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
        } catch(JSONException e) { System.out.println("bad message"); }
    }

    @Override
    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) {
        if(null != on_close)
            on_close.run();

        status = CLOSED;
    }
}
