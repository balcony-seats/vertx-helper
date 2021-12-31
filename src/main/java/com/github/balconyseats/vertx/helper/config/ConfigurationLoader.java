package com.github.balconyseats.vertx.helper.config;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vertx application configuration loader using {@link ConfigRetrieverOptions}.
 */
public class ConfigurationLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);

    private final ConfigRetrieverOptions configRetrieverOptions;

    public ConfigurationLoader(ConfigRetrieverOptions configRetrieverOptions) {
        this.configRetrieverOptions = configRetrieverOptions;
    }

    /**
     * Create instance of {@link ConfigurationLoaderBuilder}.
     * @return new instance of {see {@link ConfigurationLoaderBuilder}}
     */
    public static ConfigurationLoaderBuilder builder() {
        return new ConfigurationLoaderBuilder();
    }

    /**
     * Loads configuration.
     *
     * @return {@link Future} with configuration result
     */
    public Future<JsonObject> load() {
        Vertx vertx = Vertx.vertx();
        Promise<JsonObject> promise = Promise.promise();
        ConfigRetriever.create(vertx, this.configRetrieverOptions)
                .getConfig()
                .onSuccess(c -> {
                    LOGGER.debug("Successfully loaded configuration.");
                    vertx.close();
                    promise.complete(c);
                })
                .onFailure(cause -> {
                    LOGGER.debug("Error when loading configuration.", cause);
                    vertx.close();
                    promise.fail(cause);
                });

        return promise.future();
    }

}
