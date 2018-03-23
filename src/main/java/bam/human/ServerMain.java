package bam.human;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        // experimentServer();
        testServer();
    }

    // The real server code will go here
    private static void experimentServer() {

    }

    // Test server code will go here
    private static void testServer() throws IOException {
        Users users = Users.builder()
                .maxUsers(2)
                // .dataRoot(Directory.local("C:\\Users\\Tyler\\Desktop\\server_test"))
                .dataRoot(Directory.dummy("bam_server"))
                .sessions(ConfigurationFactory.test())
                .build();

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(Handlers.path()
                        .addPrefixPath("/", Handlers
                                .websocket((WebSocketHttpExchange exchange, WebSocketChannel channel) -> {
                                    users.add(WebsocketConnection.with(channel, 5000000L));
                                })))
                .build();
        server.start();
    }
}
