package io.github.balconyseats.vertx.helper.application.configurer.options;

import io.github.balconyseats.vertx.helper.application.configurer.VertxOptionsConfigurer;
import io.github.balconyseats.vertx.helper.application.metrics.MetricsConfigHelper;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

/**
 * Configure {@link io.vertx.core.metrics.MetricsOptions} using configuration data and add it to vertxOptions
 */
public class ConfigMetricsOptionsConfigurer implements VertxOptionsConfigurer {

    @Override
    public VertxOptions configure(VertxOptions vertxOptions, JsonObject config) {
        return vertxOptions.setMetricsOptions(MetricsConfigHelper.createMetricOptions(config));
    }


}
