package com.github.balconyseats.vertx.helper.http.handlers;

import com.github.balconyseats.vertx.helper.http.RouterHandler;
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

    public static final String CONFIG_HTTP_HEALTH_ENABLED = "/http/health/enabled";
    public static final String CONFIG_HTTP_HEALTH_PATH = "/http/health/path";

    public static final String DEFAULT_HEALTH_PATH = "/health";

    @Override
    public void apply(Vertx vertx, Router route, JsonObject config) {
        if (ConfigUtil.getBoolean(CONFIG_HTTP_HEALTH_ENABLED, config)) {
            DefaultHealthCheckHandler.forPath(ConfigUtil.getString(CONFIG_HTTP_HEALTH_PATH, config, DEFAULT_HEALTH_PATH))
                .apply(vertx, route, config);
        }
    }

}