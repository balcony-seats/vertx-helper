package io.github.balconyseats.vertx.helper.application.configurer;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Custom configurer for {@link VertxOptions}.
 */
@FunctionalInterface
public interface VertxOptionsConfigurer {

    /**
     * Configures vertx options.
     *
     * @param vertxOptions options to configure
     * @param config configuration object
     * @return vertxOptions argument
     */
    VertxOptions configure(VertxOptions vertxOptions, JsonObject config);

    static VertxOptionsConfigurer composite(VertxOptionsConfigurer... configurers) {
        return new CompositeVertxOptionsConfigurer(List.of(configurers));
    }

}
