package com.github.balconyseats.vertx.helper.application.metrics;

import com.github.balconyseats.vertx.helper.util.ConfigUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.ext.web.Router;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.PrometheusScrapingHandler;
import io.vertx.micrometer.VertxPrometheusOptions;

/**
 * Create metrics components from config:
 * <pre>
 *     metrics:
 *       micrometer:
 *          enabled: true
 *          prometheus:
 *            path: '/metrics'
 *            enabled: true
 * </pre>
 *
 */
public class MetricsConfigHelper {

    public static final String METRICS_MICROMETER_ENABLED = "/metrics/micrometer/enabled";
    public static final String METRICS_MICROMETER_PROMETHEUS_ENABLED = "/metrics/micrometer/prometheus/enabled";
    public static final String METRICS_MICROMETER_PROMETHEUS_PATH = "/metrics/micrometer/prometheus/path";

    /**
     * Add metrics handler to http server router
     * @param vertx vertx instance
     * @param router router instance
     * @param config configuration
     */
    public static void addMetricsHandler(Vertx vertx, Router router, JsonObject config) {
        if (metricsEnabled(config) && prometheusEnabled(config)) {
            String path = ConfigUtil.getString(METRICS_MICROMETER_PROMETHEUS_PATH, config, "/metrics");
            router.get(path).handler(PrometheusScrapingHandler.create());
        }
    }

    /**
     * Create metric options for Vertx instance
     * @param config configuration
     * @return created {@link MetricsOptions} instance
     */
    public static MetricsOptions createMetricOptions(JsonObject config) {
        if (ConfigUtil.getBoolean("/metrics/micrometer/enabled", config)) {
            final MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions()
                .setJvmMetricsEnabled(true)
                .setEnabled(true);

            if (ConfigUtil.getBoolean("/metrics/micrometer/prometheus/enabled", config)) {
                metricsOptions.setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true));
            }

            return metricsOptions;
        }

        return null;
    }

    private static boolean metricsEnabled(JsonObject config) {
        return ConfigUtil.getBoolean(METRICS_MICROMETER_ENABLED, config);
    }

    private static boolean prometheusEnabled(JsonObject config) {
        return ConfigUtil.getBoolean(METRICS_MICROMETER_PROMETHEUS_ENABLED, config);
    }
}
