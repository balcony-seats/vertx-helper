package com.github.balconyseats.vertx.helper.application;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class VertxContext {
    private final Vertx vertx;
    private final InitializationContext initializationContext;
    private final JsonObject configuration;

    private VertxContext(Vertx vertx, InitializationContext initializationContext, JsonObject configuration) {
        this.vertx = vertx;
        this.initializationContext = initializationContext;
        this.configuration = configuration;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public InitializationContext getInitializationContext() {
        return initializationContext;
    }

    public JsonObject getConfiguration() {
        return configuration;
    }

    public static VertxContext of(Vertx vertx, InitializationContext initializationContext, JsonObject configuration) {
        return new VertxContext(vertx, initializationContext, configuration);
    }

}
