package io.github.balconyseats.vertx.helper.http.handlers;

import io.github.balconyseats.vertx.helper.http.RouterHandler;
import io.github.balconyseats.vertx.helper.util.ConfigUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Route handler that configures health check using configuration data:
 *
 * <pre>
 *     http:
 *       health:
 *         enabled: true
 *         path: '/health'
 * </pre>
 *
 * If 'path' is not defined then '/health' is used.
 */
public class ConfigHealthCheckHandler implements RouterHandler {

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
