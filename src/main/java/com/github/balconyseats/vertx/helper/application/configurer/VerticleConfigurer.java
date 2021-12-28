package com.github.balconyseats.vertx.helper.application.configurer;

import com.github.balconyseats.vertx.helper.application.InitializationContext;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

/**
 * Instantiate and configures new {see {@link Verticle}}
 */
@FunctionalInterface
public interface VerticleConfigurer {

    Verticle create(InitializationContext initializationContext, JsonObject config);

}
