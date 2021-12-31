package io.github.balconyseats.vertx.helper.http;

import io.vertx.core.json.JsonObject;

public interface Configurer<T> {

    T configure(JsonObject config);

}
