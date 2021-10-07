package com.github.balconyseats.vertx.application.support;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.tracing.TracingOptions;

import java.util.Optional;

/**
 * Common Vert.X application creator
 */
public class VertxApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertxApplication.class);

    private final JsonObject config;

    private final Vertx vertx;

    public VertxApplication(JsonObject config) {
        this.config = config;

        VertxOptions vertxOptions = new VertxOptions();
        configureMetricsOptions().ifPresent(vertxOptions::setMetricsOptions);
        configureTracingOptions().ifPresent(vertxOptions::setTracingOptions);

        this.vertx = Vertx.vertx(vertxOptions);
    }

    /**
     * Run application
     * @return future with run status
     */
    public Future<Void> run() {
        return this.deployVertices();
    }

    /**
     * Method to deploy vertices
     * @return
     */
    protected Future<Void> deployVertices() {
        return Promise.<Void>promise().future();
    }

    /**
     * Adds additional vertx options if needed
     * @param vertxOptions
     * @return
     */
    protected VertxOptions addVertxOptions(VertxOptions vertxOptions) {
        return vertxOptions;
    }

    /**
     * Configure metrics options for vertx.
     * @return empty or optional with configured metric options
     */
    protected Optional<MetricsOptions> configureMetricsOptions() {
        return Optional.empty();
    }

    /**
     * Configure tracing options for vertx.
     * @return empty or optional with configured tracing options
     */
    protected Optional<TracingOptions> configureTracingOptions() {
        return Optional.empty();
    }


}
