package com.github.balconyseats.vertx.helper.application.http.handlers;

import com.github.balconyseats.vertx.helper.application.http.RouterHandler;
import com.github.balconyseats.vertx.helper.util.ConfigUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Configures health check from configuration:
 *
 * <pre>
 *     http:
 *       health:
 *         enabled: true
 *         path: '/health'
 * </pre>
 *
 * I path is not defined then '/health' path is used
 */
public class ConfigurationHealthCheckHandler implements RouterHandler {

    @Override
    public void apply(Vertx vertx, Router route, JsonObject config) {
        if (ConfigUtil.getBoolean("/http/health/enabled", config)) {
            DefaultHealthCheckHandler.forPath(ConfigUtil.getString("/http/health/path", config, "/health"))
                .apply(vertx, route, config);
        }
    }

}
