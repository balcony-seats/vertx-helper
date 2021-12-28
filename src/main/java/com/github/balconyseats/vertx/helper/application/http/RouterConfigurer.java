package com.github.balconyseats.vertx.helper.application.http;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.tuple.Pair;


@FunctionalInterface
public interface RouterConfigurer {

    Future<Pair<String, Router>> configure(JsonObject jsonObject);

}
