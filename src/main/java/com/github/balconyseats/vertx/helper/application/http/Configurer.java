package com.github.balconyseats.vertx.helper.application.http;

import io.vertx.core.json.JsonObject;

public interface Configurer<T> {

    T configure(JsonObject config);

}
