package bam.human;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import java.io.IOException;
import java.net.InetAddress;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        // experimentServer();
        testServer();
    }

    // The real server code will go here
    private static void experimentServer() throws IOException {
        Users users = Users.builder()
                .maxUsers(16)
                .dataRoot(Directory.local(System.getProperty("user.home")))
                .sessions(ConfigurationFactory.experiment())
                .build();

        Undertow server = Undertow.builder()
                .addHttpListener(8765, "localhost")
                .setHandler(Handlers.path()
                        .addPrefixPath("/", Handlers
                                .websocket((WebSocketHttpExchange exchange, WebSocketChannel channel) -> {
                                    users.add(WebsocketConnection.with(channel, 5000000L));
                                })))
                .build();
        server.start();
    }

    // Test server code will go here
    private static void testServer() throws IOException {
        Users users = Users.builder()
                .maxUsers(2)
                .dataRoot(Directory.local("C:\\Users\\Tyler\\Desktop\\server_test"))
                .sessions(ConfigurationFactory.experiment())
                .build();

        Undertow server = Undertow.builder()
                .addHttpListener(8765, "localhost")
                .setHandler(Handlers.path()
                        .addPrefixPath("/", Handlers
                                .websocket((WebSocketHttpExchange exchange, WebSocketChannel channel) -> {
                                    users.add(WebsocketConnection.with(channel, 5000000L));
                                })))
                .build();
        server.start();
    }
}
