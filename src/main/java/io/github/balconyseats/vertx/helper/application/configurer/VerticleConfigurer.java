package io.github.balconyseats.vertx.helper.application.configurer;

import io.github.balconyseats.vertx.helper.application.InitializationContext;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

/**
 * Configurer that instantiates and configures new {see {@link Verticle}}.
 */
@FunctionalInterface
public interface VerticleConfigurer {

    Verticle create(InitializationContext initializationContext, JsonObject config);

}
