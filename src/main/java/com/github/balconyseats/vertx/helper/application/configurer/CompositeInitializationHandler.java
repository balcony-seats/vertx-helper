package com.github.balconyseats.vertx.helper.application.configurer;

import com.github.balconyseats.vertx.helper.application.InitializationContext;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CompositeInitializationHandler implements InitializationHandler {

    private final List<InitializationHandler> processors;

    public CompositeInitializationHandler(List<InitializationHandler> processors) {
        this.processors = Objects.requireNonNullElseGet(processors, ArrayList::new);
    }

    @Override
    public Future<Void> handle(Vertx vertx, InitializationContext initializationContext, JsonObject config) {

        Promise<Void> promise = Promise.promise();

        List<Future<Void>> futures = processors.stream()
            .map(p -> p.handle(vertx, initializationContext, config))
            .collect(Collectors.toList());

        CompositeFuture.join(Collections.unmodifiableList(futures))
            .onSuccess((__) -> promise.complete())
            .onFailure(promise::fail);

        return promise.future();
    }
}
