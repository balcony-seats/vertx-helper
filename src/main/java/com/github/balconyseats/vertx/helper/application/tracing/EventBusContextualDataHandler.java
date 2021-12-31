package com.github.balconyseats.vertx.helper.application.tracing;

import com.github.balconyseats.vertx.helper.application.InitializationContext;
import com.github.balconyseats.vertx.helper.application.configurer.InitializationHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


public class EventBusContextualDataHandler implements InitializationHandler {

    @Override
    public Future<Void> handle(Vertx vertx, InitializationContext initializationContext, JsonObject config) {
        Promise<Void> promise = Promise.promise();
        try {
            TracingConfigHelper.contextualDataForEventBus(vertx, config);
            promise.complete();
        } catch (Throwable t) {
            promise.fail(t);
        }
        return promise.future();
    }
}
