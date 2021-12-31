package com.github.balconyseats.vertx.helper.http.handlers;

import com.github.balconyseats.vertx.helper.http.RouterHandler;
import com.github.balconyseats.vertx.helper.application.tracing.TracingConfigHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Route handler that adds 'spanId' and 'traceId' contextual logging data.
 */
public class ContextualLoggingRouterHandler implements RouterHandler {

    @Override
    public void apply(Vertx vertx, Router router, JsonObject config) {
        router.route().handler(routingContext -> {
            TracingConfigHelper.setContextualData(config);
            routingContext.next();
        });
    }

}
