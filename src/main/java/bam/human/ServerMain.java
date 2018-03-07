package bam.human;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import java.io.File;
import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) {
        testServer();
    }

    // The real server code will go here
    private static void runServer() {

    }

    // Test server code will go here
    private static void testServer() {
        UserGroup users = UserGroup.builder()
                .maxUsers(2)
                .dataRoot(new File("C:\\Users\\Tyler\\Desktop\\server_test"))
                .build();

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(Handlers.path()
                        .addPrefixPath("/",
                                Handlers.websocket((WebSocketHttpExchange exchange, WebSocketChannel channel) -> {
                                    try {
                                        users.add(channel);

                                        System.out.println("connection made");
                                        WebSockets.sendText("ready", channel, null);
                                    } catch(UserGroup.BusyException e) {
                                        WebSockets.sendText("busy", channel, null);
                                        System.out.println("I'm too busy");
                                    } catch(Exception e) {
                                        WebSockets.sendText("error", channel, null);
                                        System.out.println("something went really wrong");
                                    }

                                    channel.resumeReceives();
                                })))
                .build();
        server.start();
    }
}
