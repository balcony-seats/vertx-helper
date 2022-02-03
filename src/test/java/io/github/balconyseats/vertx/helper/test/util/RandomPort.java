package io.github.balconyseats.vertx.helper.test.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Utility to return random free port
 */
public class RandomPort {

    /**
     * Return local free port
     * @return port
     */
    public static int port() {
        try (var serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
