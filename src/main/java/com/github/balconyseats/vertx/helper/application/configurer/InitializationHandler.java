package com.github.balconyseats.vertx.helper.application.configurer;

import com.github.balconyseats.vertx.helper.application.InitializationContext;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface InitializationHandler {

    Future<Void> handle(Vertx vertx, InitializationContext initializationContext, JsonObject config);

    static InitializationHandler composite(InitializationHandler... handlers) {
        return new CompositeInitializationHandler(List.of(handlers));
    }

}
