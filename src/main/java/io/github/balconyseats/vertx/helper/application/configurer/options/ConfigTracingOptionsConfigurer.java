package io.github.balconyseats.vertx.helper.application.configurer.options;

import io.github.balconyseats.vertx.helper.application.configurer.VertxOptionsConfigurer;
import io.github.balconyseats.vertx.helper.application.tracing.TracingConfigHelper;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

/**
 * Configures {@link io.vertx.core.tracing.TracingOptions} from configuration
 */
public class ConfigTracingOptionsConfigurer implements VertxOptionsConfigurer {

    @Override
    public VertxOptions configure(VertxOptions vertxOptions, JsonObject config) {
        return vertxOptions.setTracingOptions(TracingConfigHelper.createTracingOptions(config));
    }

}
