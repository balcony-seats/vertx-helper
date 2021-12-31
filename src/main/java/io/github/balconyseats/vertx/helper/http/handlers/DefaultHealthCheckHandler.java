package io.github.balconyseats.vertx.helper.http.handlers;

import io.github.balconyseats.vertx.helper.http.RouterHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;

public class DefaultHealthCheckHandler implements RouterHandler {

    private final String path;

    public DefaultHealthCheckHandler(String path) {
        this.path = path;
    }

    @Override
    public void apply(Vertx vertx, Router router, JsonObject config) {
        HealthChecks healthChecks = HealthChecks.create(vertx);
        healthChecks.register("status", p -> p.complete(Status.OK()));
        HealthCheckHandler healthCheckHandler = HealthCheckHandler.createWithHealthChecks(healthChecks);
        router.get(path).handler(healthCheckHandler);
    }

    public static DefaultHealthCheckHandler forPath(String path) {
        return new DefaultHealthCheckHandler(path);
    }
}
