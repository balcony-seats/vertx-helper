package com.github.balconyseats.vertx.helper.application.configurer.metrics;

import com.github.balconyseats.vertx.helper.application.configurer.VertxOptionsConfigurer;
import com.github.balconyseats.vertx.helper.application.metrics.MetricsConfigHelper;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

/**
 * Configure metrics from configuration
 */
public class ConfigurationMetricsOptionsConfigurer implements VertxOptionsConfigurer {

    @Override
    public VertxOptions configure(VertxOptions vertxOptions, JsonObject config) {
        return vertxOptions.setMetricsOptions(MetricsConfigHelper.createMetricOptions(config));
    }


}
