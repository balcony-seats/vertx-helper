package com.github.balconyseats.vertx.helper.application.configurer;

import com.github.balconyseats.vertx.helper.application.InitializationContext;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Configure {@link InitializationContext}
 */
@FunctionalInterface
public interface InitializationContextConfigurer {

    /**
     * Configure provided initialization context
     *
     * @param initializationContext {@link InitializationContext} to configure
     * @param vertx {@link Vertx} instance
     * @param config configuration
     * @return {@link Future} with configured {@link InitializationContext}
     */
    Future<InitializationContext> configure(InitializationContext initializationContext, Vertx vertx, JsonObject config);

    /**
     * Instantiate new instance of {@link CompositeInitializationContextConfigurer}
     * @param configurers array of configurers {@link InitializationContextConfigurer}
     * @return {@link CompositeInitializationContextConfigurer}
     */
    static InitializationContextConfigurer composite(InitializationContextConfigurer... configurers) {
        return new CompositeInitializationContextConfigurer(List.of(configurers));
    }

}
