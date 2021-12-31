package io.github.balconyseats.vertx.helper.application.metrics;

import io.github.balconyseats.vertx.helper.util.ConfigUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.ext.web.Router;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.PrometheusScrapingHandler;
import io.vertx.micrometer.VertxPrometheusOptions;

/**
 * Helper class for creating metrics components using configuration data:
 * <pre>
 *     metrics:
 *       micrometer:
 *          enabled: true
 *          prometheus:
 *            path: '/metrics'
 *            enabled: true
 * </pre>
 */
public class MetricsConfigHelper {

    public static final String CONFIG_METRICS_MICROMETER_ENABLED = "/metrics/micrometer/enabled";
    public static final String CONFIG_METRICS_MICROMETER_PROMETHEUS_ENABLED = "/metrics/micrometer/prometheus/enabled";
    public static final String CONFIG_METRICS_MICROMETER_PROMETHEUS_PATH = "/metrics/micrometer/prometheus/path";

    public static final String DEFAULT_METRICS_PATH = "/metrics";

    /**
     * Adds metrics handler to http server router.
     * @param vertx vertx instance
     * @param router router instance
     * @param config configuration
     */
    public static void addMetricsHandler(Vertx vertx, Router router, JsonObject config) {
        if (metricsEnabled(config) && prometheusEnabled(config)) {
            String path = ConfigUtil.getString(CONFIG_METRICS_MICROMETER_PROMETHEUS_PATH, config, DEFAULT_METRICS_PATH);
            router.get(path).handler(PrometheusScrapingHandler.create());
        }
    }

    /**
     * Creates metric options for Vertx instance.
     * @param config configuration
     * @return created {@link MetricsOptions} instance
     */
    public static MetricsOptions createMetricOptions(JsonObject config) {
        if (metricsEnabled(config)) {
            final MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions()
                .setJvmMetricsEnabled(true)
                .setEnabled(true);

            if (prometheusEnabled(config)) {
                metricsOptions.setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true));
            }

            return metricsOptions;
        }

        return null;
    }

    private static boolean metricsEnabled(JsonObject config) {
        return ConfigUtil.getBoolean(CONFIG_METRICS_MICROMETER_ENABLED, config);
    }

    private static boolean prometheusEnabled(JsonObject config) {
        return ConfigUtil.getBoolean(CONFIG_METRICS_MICROMETER_PROMETHEUS_ENABLED, config);
    }
}
