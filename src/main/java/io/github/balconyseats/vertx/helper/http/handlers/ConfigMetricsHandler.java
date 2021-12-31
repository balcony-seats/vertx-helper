package io.github.balconyseats.vertx.helper.http.handlers;

import io.github.balconyseats.vertx.helper.http.RouterHandler;
import io.github.balconyseats.vertx.helper.application.metrics.MetricsConfigHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Creates metrics route handler from configuration. For details look at {@link MetricsConfigHelper}
 */
public class ConfigMetricsHandler implements RouterHandler {

    @Override
    public void apply(Vertx vertx, Router router, JsonObject config) {
        MetricsConfigHelper.addMetricsHandler(vertx, router, config);
    }

}
