package io.github.balconyseats.vertx.helper.application.configurer;

import io.github.balconyseats.vertx.helper.application.InitializationContext;
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

public class CompositeInitializationContextConfigurer implements InitializationContextConfigurer {

    private final List<InitializationContextConfigurer> configurers;

    public CompositeInitializationContextConfigurer(List<InitializationContextConfigurer> configurers) {
        this.configurers = Objects.requireNonNullElseGet(configurers, ArrayList::new);
    }

    @Override
    public Future<InitializationContext> configure(InitializationContext initializationContext, Vertx vertx, JsonObject config) {
        Promise<InitializationContext> promise = Promise.promise();

        List<Future<InitializationContext>> futures = configurers.stream()
            .map(p -> p.configure(initializationContext, vertx, config))
            .collect(Collectors.toList());

        CompositeFuture.join(Collections.unmodifiableList(futures))
            .onSuccess((__) -> promise.complete(initializationContext))
            .onFailure(promise::fail);

        return promise.future();
    }
}
