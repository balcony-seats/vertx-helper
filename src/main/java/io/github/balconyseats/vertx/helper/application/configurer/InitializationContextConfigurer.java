package io.github.balconyseats.vertx.helper.application.configurer;

import io.github.balconyseats.vertx.helper.application.InitializationContext;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * {@link InitializationContext} configurer.
 */
@FunctionalInterface
public interface InitializationContextConfigurer {

    /**
     * Configures provided initialization context.
     *
     * @param initializationContext {@link InitializationContext} to configure
     * @param vertx {@link Vertx} instance
     * @param config configuration
     * @return {@link Future} with configured {@link InitializationContext}
     */
    Future<InitializationContext> configure(InitializationContext initializationContext, Vertx vertx, JsonObject config);

    /**
     * Instantiates new instance of {@link CompositeInitializationContextConfigurer}.
     * @param configurers array of {@link InitializationContextConfigurer}
     * @return {@link CompositeInitializationContextConfigurer}
     */
    static InitializationContextConfigurer composite(InitializationContextConfigurer... configurers) {
        return new CompositeInitializationContextConfigurer(List.of(configurers));
    }

}
