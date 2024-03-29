package io.github.balconyseats.vertx.helper.http;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouterHandler {

    void apply(Vertx vertx, Router router, JsonObject config);

}
