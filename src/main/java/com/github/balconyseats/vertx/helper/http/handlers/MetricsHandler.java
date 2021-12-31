package com.github.balconyseats.vertx.helper.http.handlers;

import com.github.balconyseats.vertx.helper.http.RouterHandler;
import com.github.balconyseats.vertx.helper.application.metrics.MetricsConfigHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MetricsHandler implements RouterHandler {

    @Override
    public void apply(Vertx vertx, Router router, JsonObject config) {
        MetricsConfigHelper.addMetricsHandler(vertx, router, config);
    }

}