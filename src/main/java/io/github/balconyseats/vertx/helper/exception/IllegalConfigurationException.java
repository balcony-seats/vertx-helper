package io.github.balconyseats.vertx.helper.exception;

public class IllegalConfigurationException extends RuntimeException {

    public IllegalConfigurationException(String message) {
        super(message);
    }

    public IllegalConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
