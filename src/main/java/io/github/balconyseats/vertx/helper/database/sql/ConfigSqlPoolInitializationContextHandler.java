package io.github.balconyseats.vertx.helper.database.sql;

import io.github.balconyseats.vertx.helper.application.InitializationContext;
import io.github.balconyseats.vertx.helper.application.configurer.InitializationHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Creates {@link io.vertx.sqlclient.Pool} from configuration and
 * adds it to initialization context under "sqlpool" key.
 */
public class ConfigSqlPoolInitializationContextHandler implements InitializationHandler {

    public static final String SQLPOOL_KEY = "sqlpool";

    private final String key;

    private ConfigSqlPoolInitializationContextHandler() {
        this(SQLPOOL_KEY);
    }

    private ConfigSqlPoolInitializationContextHandler(String key) {
        this.key = key;
    }

    @Override
    public Future<Void> handle(Vertx vertx, InitializationContext initializationContext, JsonObject config) {
        Promise<Void> promise = Promise.promise();

        initializationContext.add(this.key, ConfigSqlPoolHelper.create(vertx, config));

        promise.complete();
        return promise.future();
    }

    public static ConfigSqlPoolInitializationContextHandler instance() {
        return new ConfigSqlPoolInitializationContextHandler();
    }

    public static ConfigSqlPoolInitializationContextHandler instance(String key) {
        return new ConfigSqlPoolInitializationContextHandler(key);
    }
}
