package io.github.balconyseats.vertx.helper.database.sql;

import io.github.balconyseats.vertx.helper.application.InitializationContext;
import io.github.balconyseats.vertx.helper.application.configurer.InitializationContextConfigurer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Creates {@link io.vertx.sqlclient.Pool} from configuration and
 * adds it to initialization context under "sqlpool" key.
 */
public class ConfigSqlPoolInitializationContextHandler implements InitializationContextConfigurer {

    public static final String SQLPOOL_KEY = "sqlpool";

    private final String key;

    private ConfigSqlPoolInitializationContextHandler() {
        this(SQLPOOL_KEY);
    }

    private ConfigSqlPoolInitializationContextHandler(String key) {
        this.key = key;
    }

    @Override
    public Future<InitializationContext> configure(InitializationContext initializationContext, Vertx vertx, JsonObject config) {
        return Future.future(p -> {
            initializationContext.add(this.key, ConfigSqlPoolHelper.create(vertx, config));
            p.complete(initializationContext);
        });
    }

    public static ConfigSqlPoolInitializationContextHandler instance() {
        return new ConfigSqlPoolInitializationContextHandler();
    }

    public static ConfigSqlPoolInitializationContextHandler instance(String key) {
        return new ConfigSqlPoolInitializationContextHandler(key);
    }
}
