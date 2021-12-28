package com.github.balconyseats.vertx.helper.application.configurer.metrics;

import com.github.balconyseats.vertx.helper.application.configurer.VertxOptionsConfigurer;
import com.github.balconyseats.vertx.helper.application.tracing.TracingConfigHelper;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class ConfigurationTracingOptionsConfigurer implements VertxOptionsConfigurer {

    @Override
    public VertxOptions configure(VertxOptions vertxOptions, JsonObject config) {
        return vertxOptions.setTracingOptions(TracingConfigHelper.createTracingOptions(config));
    }

}
