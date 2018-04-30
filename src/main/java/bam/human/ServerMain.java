package bam.human;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import java.io.IOException;
import java.nio.file.Paths;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        // experimentServer();
        testServer();
    }

    // The real server code will go here
    private static void experimentServer() throws IOException {

        Pool users = Pool.maxItems(16);

        Study web = Study.builder()
                .pool(users)
                .dataRoot(Directory.local(Paths.get(System.getProperty("user.home")).resolve("web")))
                .sessions(ConfigurationFactory.experiment())
                .codes(CodeFactory.uuid())
                .build();

        Study mturk = Study.builder()
                .pool(users)
                .dataRoot(Directory.local(Paths.get(System.getProperty("user.home")).resolve("mturk")))
                .sessions(ConfigurationFactory.experiment())
                .codes(CodeFactory.uuid())
                .build();

        Undertow server = Undertow.builder()
                .addHttpListener(8215, "localhost", Handlers.path()
                        .addPrefixPath("/", Handlers
                                .websocket((WebSocketHttpExchange exchange, WebSocketChannel channel) -> {
                                    web.add(WebsocketConnection.with(channel, 8000000L));
                                })))
                .addHttpListener(8217, "localhost", Handlers.path()
                        .addPrefixPath("/", Handlers
                                .websocket((WebSocketHttpExchange exchange, WebSocketChannel channel) -> {
                                    mturk.add(WebsocketConnection.with(channel, 8000000L));
                                })))
                .build();
        server.start();
    }

    // Test server code will go here
    private static void testServer() throws IOException {

        Pool users = Pool.maxItems(4);

        Study mturk = Study.builder()
                .pool(users)
                .dataRoot(Directory.local("C:\\Users\\Tyler\\Desktop\\server_test\\mturk"))
                .sessions(ConfigurationFactory.experiment())
                .codes(CodeFactory.uuid())
                .build();

        Study web = Study.builder()
                .pool(users)
                .dataRoot(Directory.local("C:\\Users\\Tyler\\Desktop\\server_test\\web"))
                .sessions(ConfigurationFactory.experiment())
                .codes(CodeFactory.uuid())
                .build();

        Undertow server = Undertow.builder()
                .addHttpListener(8215, "localhost", Handlers.path()
                        .addPrefixPath("/", Handlers
                                .websocket((WebSocketHttpExchange exchange, WebSocketChannel channel) -> {
                                    web.add(WebsocketConnection.with(channel, 5000000L));
                                })))
                .addHttpListener(8217, "localhost", Handlers.path()
                        .addPrefixPath("/", Handlers
                                .websocket((WebSocketHttpExchange exchange, WebSocketChannel channel) -> {
                                    mturk.add(WebsocketConnection.with(channel, 5000000L));
                                })))
                .build();
        server.start();
    }
}
